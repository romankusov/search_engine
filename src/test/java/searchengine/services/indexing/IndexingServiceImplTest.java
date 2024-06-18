package searchengine.services.indexing;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class IndexingServiceImplTest {
    private final String FIRST_SITE_URL = "http://url.com/";
    private final SiteRepository siteRepository = mock(SiteRepository.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final LemmaRepository lemmaRepository = mock(LemmaRepository.class);
    private final IndexRepository indexRepository = mock(IndexRepository.class);

    @Test
    @DisplayName("StartIndexing Test")
    @SneakyThrows
    public void startIndexingTest_start()
    {
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl(FIRST_SITE_URL);
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXING);

        PageEntity testPage = new PageEntity();
        testPage.setSiteEntity(siteEntity);
        testPage.setCode(200);
        testPage.setContent("контент");
        testPage.setId(1);

        LemmaEntity lemmaEntity1 = new LemmaEntity(1, siteEntity, "сено", 1);
        LemmaEntity lemmaEntity2 = new LemmaEntity(2, siteEntity, "твид", 1);
        LemmaEntity lemmaEntity3 = new LemmaEntity(3, siteEntity, "ультрафиолет", 1);
        LemmaEntity lemmaEntity4 = new LemmaEntity(4, siteEntity, "филин", 1);
        List<LemmaEntity> lemmaEntityList = List.of(lemmaEntity1, lemmaEntity2, lemmaEntity3, lemmaEntity4);

        List<IndexEntity> indexEntityList = new ArrayList<>();
        List<Integer> rankList = List.of(20,16,18,24);
        int i = 1;
        for (LemmaEntity lemmaEntity : lemmaEntityList) {
            for (Integer integer : rankList) {
                IndexEntity indexEntity = new IndexEntity(i, testPage, lemmaEntity,
                        integer);
                indexEntityList.add(indexEntity);
                i++;
            }
        }

        when(siteRepository.save(siteEntity)).thenReturn(siteEntity);
        when(siteRepository.save(any(SiteEntity.class))).thenReturn(siteEntity);
        when(pageRepository.save(testPage)).thenReturn(testPage);
        when(lemmaRepository.saveAll(lemmaEntityList)).thenReturn(lemmaEntityList);
        when(indexRepository.saveAll(indexEntityList)).thenReturn(indexEntityList);

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);

        IndexingResponse actualResponse = indexingService.startIndexing();

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("StartIndexing Test if is indexing now")
    @SneakyThrows
    public void startIndexingTest_ifIsIndexing()
    {
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl(FIRST_SITE_URL);
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXING);

        when(siteRepository.findAll()).thenReturn(List.of(siteEntity));

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Индексация уже запущена");

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);

        IndexingResponse actualResponse = indexingService.startIndexing();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("StopIndexing Test if is indexing")
    public void stopIndexingTest_ifIsIsIndexing()
    {
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl(FIRST_SITE_URL);
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXING);

        when(siteRepository.findAll()).thenReturn(List.of(siteEntity));

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);

        IndexingResponse actualResponse = indexingService.stopIndexing();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("StopIndexing Test if is not indexing")
    public void stopIndexingTest_ifIsNotIndexing()
    {
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl(FIRST_SITE_URL);
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXED);

        when(siteRepository.findAll()).thenReturn(List.of(siteEntity));

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Индексация не запущена");

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);

        IndexingResponse actualResponse = indexingService.stopIndexing();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @SneakyThrows
    @DisplayName("IndexPage with proper url")
    public void indexPageTest()
    {
        String inUrl = "http://url.com/url";
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl("http://url.com");
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXED);

        PageEntity testPage = new PageEntity();
        testPage.setPath("/url");
        testPage.setSiteEntity(siteEntity);
        testPage.setCode(200);
        testPage.setContent("контент");
        testPage.setId(1);

        when(siteRepository.save(siteEntity)).thenReturn(siteEntity);
        when(siteRepository.findByUrl(FIRST_SITE_URL)).thenReturn(Optional.of(siteEntity));
        when(pageRepository.save(any(PageEntity.class))).thenReturn(testPage);
        when(pageRepository.saveAndFlush(any(PageEntity.class))).thenReturn(testPage);

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);
        IndexingResponse actualResponse = indexingService.indexPage(inUrl);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @SneakyThrows
    @DisplayName("IndexPage with wrong url Test")
    public void indexPage_wrongUrl_Test()
    {
        String inUrl = "http://wrong_url.com/url";
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        List<Site> siteList = new ArrayList<>();
        siteList.add(site);
        SitesList testSiteList = new SitesList();
        testSiteList.setSites(siteList);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl("http://url.com");
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXED);

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Данная страница находится за пределами сайтов, " +
                                    "указанных в конфигурационном файле");

        IndexingServiceImpl indexingService = new IndexingServiceImpl(pageRepository, siteRepository,
                lemmaRepository, indexRepository, testSiteList);
        IndexingResponse actualResponse = indexingService.indexPage(inUrl);

        assertEquals(expectedResponse, actualResponse);
    }
}
