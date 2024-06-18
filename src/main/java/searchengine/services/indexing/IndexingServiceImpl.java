package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.IndexEntity;
import searchengine.model.IndexStatus;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.JsoupWorks;
import searchengine.utils.LemmasAndIndexesEntMaker;
import searchengine.utils.SiteParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private volatile PageRepository pageRepository;

    @Autowired
    private volatile SiteRepository siteRepository;

    @Autowired
    private volatile LemmaRepository lemmaRepository;

    @Autowired
    private volatile IndexRepository indexRepository;

    private final SitesList sitesList;

    @Override
    public IndexingResponse startIndexing() throws InterruptedException
    {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing())
        {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else
        {
            List <Thread> tasks = new ArrayList<>();
            deleteAllEntities();
            log.info("Indexing begins, entities after delete: " + siteRepository.findAll().size());
            SiteParser.setStop(false);
            for (Site site : sitesList.getSites())
            {
                Thread thread = new Thread(new SiteParser(siteRepository, pageRepository,
                        lemmaRepository, indexRepository, site));
                tasks.add(thread);
            }
            tasks.forEach(Thread::start);
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexingResponse stopIndexing()
    {
        IndexingResponse response = new IndexingResponse();
        if (!isIndexing())
        {
            response.setResult(false);
            response.setError("Индексация не запущена");
        } else
        {
            SiteParser.setStop(true);
            response.setResult(true);
        }
        return response;
    }

    @Override
    @Transactional
    public IndexingResponse indexPage(String url) throws Exception
    {
        IndexingResponse response = new IndexingResponse();

        List<Site> siteUrlList = sitesList.getSites().stream()
                .filter(s -> url.contains(s.getUrl())).toList();

        if (siteUrlList.isEmpty())
        {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        } else
        {
            Site site = siteUrlList.get(0);
            if (siteRepository.findByUrl(site.getUrl()).isEmpty())
            {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(IndexStatus.FAILED);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
            }
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl()).get();
            PageEntity page = JsoupWorks.makeOnePageForDB(siteEntity, url);
            if (pageRepository.findByPathAndSiteEntityUrl(page.getPath(), siteEntity.getUrl()).isPresent())
            {
                PageEntity pageForDelete = pageRepository.findByPathAndSiteEntityUrl(page.getPath(),
                        siteEntity.getUrl()).get();
                List<LemmaEntity> lemmasByPageForDecrease = indexRepository.getLemmasByPage(pageForDelete);
                indexRepository.deleteByPageEntity(pageForDelete);
                lemmaRepository.decreaseFrequencyLemmaAndGetZeroFr(lemmasByPageForDecrease);
                pageRepository.delete(pageForDelete);
            }
            page = pageRepository.save(page);
            Map<LemmaEntity, Integer> lemmaEntityAndRankMap = new ConcurrentHashMap<>();
            ConcurrentLinkedQueue<IndexEntity> indexEntities = new ConcurrentLinkedQueue<>();
            LemmasAndIndexesEntMaker lemmasAndIndexesEntMaker =
                    new LemmasAndIndexesEntMaker(lemmaEntityAndRankMap, indexEntities);
            lemmasAndIndexesEntMaker.addLeAndIeToTempCollection(page);
            List<LemmaEntity> lemmaEntityListForSave = lemmasAndIndexesEntMaker.getLemmasListForSave();
            List<LemmaEntity> lemmaEntityListForIndex = lemmaRepository.upsertLemmas(siteEntity, lemmaEntityListForSave);
            indexEntities = lemmasAndIndexesEntMaker.getIndexEntityQueueForSave(lemmaEntityListForIndex);
            indexRepository.saveAll(indexEntities);
            response.setResult(true);
            //response.setError("");
        }
        return response;
    }

    private boolean isIndexing()
    {
        Iterable<SiteEntity> siteEntities = siteRepository.findAll();
        ArrayList<SiteEntity> siteEntityArrayList = new ArrayList<>();
        siteEntities.forEach(siteEntityArrayList::add);
        return siteEntityArrayList.stream()
                .anyMatch(e -> e.getStatus() == IndexStatus.INDEXING);
    }

    private void deleteAllEntities()
    {
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}
