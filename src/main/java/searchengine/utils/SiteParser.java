package searchengine.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.IndexEntity;
import searchengine.model.IndexStatus;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SiteParser implements Runnable {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Site site;
    private final ForkJoinPool fjp = new ForkJoinPool();
    @Getter
    private static volatile boolean stop = false;

    public SiteParser(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository,
                      IndexRepository indexRepository, Site site) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.site = site;
    }

    public static void setStop(boolean stop)
    {
        SiteParser.stop = stop;
    }

    @Override
    public void run()
    {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(IndexStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        SiteEntity insertedSE = siteRepository.save(siteEntity);

        try
        {
            String parsedLink = insertedSE.getUrl();
            Set<String> uniqueLinksSet = ConcurrentHashMap.newKeySet();
            Map<LemmaEntity, Integer> lemmaEntityAndRankMap = new ConcurrentHashMap<>();
            ConcurrentLinkedQueue<IndexEntity> indexEntities = new ConcurrentLinkedQueue<>();
            LemmasAndIndexesEntMaker lemmasAndIndexesEntMaker =
                    new LemmasAndIndexesEntMaker(lemmaEntityAndRankMap, indexEntities);

            lemmasAndIndexesEntMaker = fjp.invoke(new PageParser(parsedLink, insertedSE, pageRepository,
                    uniqueLinksSet, siteRepository, lemmasAndIndexesEntMaker));
            List<LemmaEntity> lemmaEntityListForSave = lemmasAndIndexesEntMaker.getLemmasListForSave();
            if (stop)
            {
                stopPageParsing();
                String errorMsg = "Индексация остановлена пользователем";
                siteRepository.update(insertedSE, IndexStatus.FAILED, errorMsg);
                return;
            }

            synchronized (lemmaRepository)
            {
                lemmaEntityListForSave = lemmaRepository.saveAllAndFlush(lemmaEntityListForSave);
            }
            indexEntities = lemmasAndIndexesEntMaker.getIndexEntityQueueForSave(lemmaEntityListForSave);
            synchronized (indexRepository)
            {
                indexRepository.saveAllAndFlush(indexEntities);
            }
            lemmasAndIndexesEntMaker.clear();
            log.info("Indexing stopped");

            uniqueLinksSet.clear();

            siteRepository.update(insertedSE, IndexStatus.INDEXED);
        } catch (Exception ex)
        {
            siteRepository.update(insertedSE, IndexStatus.FAILED, ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    public void stopPageParsing()
    {
       fjp.shutdown();
       try
       {
           if (fjp.awaitTermination(800, TimeUnit.MILLISECONDS))
           {
               fjp.shutdownNow();
           }
       } catch (InterruptedException e)
       {
           fjp.shutdownNow();
       }
    }
}
