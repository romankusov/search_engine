package searchengine.services.search;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResultResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.LemmasAndIndexesEntMaker;
import searchengine.utils.LemmasFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchServiceImplTest {

    private final String FIRST_SITE_URL = "http://url.com/";
    private final SiteRepository siteRepository = mock(SiteRepository.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final LemmaRepository lemmaRepository = mock(LemmaRepository.class);
    private final IndexRepository indexRepository = mock(IndexRepository.class);
    private final SitesList testSitesList = new SitesList();

    SiteEntity siteEntity = new SiteEntity();
    List<PageEntity> testPagesList = new ArrayList<>();
    List<LemmaEntity> lemmaEntityList = new ArrayList<>();
    ConcurrentLinkedQueue<IndexEntity> indexEntities = new ConcurrentLinkedQueue<>();

    private static LemmasFinder lemmasFinder;
    static {
        try {
            lemmasFinder = LemmasFinder.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    @SneakyThrows
    public void setUp()
    {
        Site site = new Site();
        site.setUrl(FIRST_SITE_URL);
        site.setName("Test Site");
        testSitesList.setSites(List.of(site));

        siteEntity = getSIteEntityForTest();
        testPagesList = getPageEntityList(siteEntity);

        LemmasAndIndexesEntMaker lemmasAndIndexesEntMaker = new LemmasAndIndexesEntMaker
                (new LinkedHashMap<>(), new ConcurrentLinkedQueue<>());

        for (PageEntity page : testPagesList)
        {
            lemmasAndIndexesEntMaker.addLeAndIeToTempCollection(page);
            //getLemmasAndIndexes(lemmasAndIndexesEntMaker, page);
            lemmaEntityList.addAll(lemmasAndIndexesEntMaker.getLemmasListForSave());
            indexEntities.addAll(lemmasAndIndexesEntMaker.getIndexEntityQueue());
        }

        int idLemma = 1;
        for (LemmaEntity lemma : lemmaEntityList)
        {
            lemma.setId(idLemma);
            idLemma++;
        }

        int idIndex = 1;

        for (IndexEntity index : indexEntities)
        {
            index.setId(idIndex);
            idIndex++;
        }
    }

    @Test
    @DisplayName("Test of search")
    public void searchTest()
    {
        LemmaEntity firstLemma = lemmaEntityList.get(0);
        List<LemmaEntity> lemmasInDb = List.of(firstLemma);
        String searchQuery = firstLemma.getLemma();// + " " + secondLemma.getLemma();
        Set<String> lemmasSet = Set.of(firstLemma.getLemma());//, secondLemma.getLemma());
        List<SiteEntity> siteEntityList = List.of(siteEntity);
        float avgFreq = getAvgFreq(lemmaEntityList);
        int maxFreqForSearch = Math.round(avgFreq);
        int maxAbsRelForTest = getMaxAbsRelForTest(lemmasInDb);

        when(siteRepository.findAll()).thenReturn(List.of(siteEntity));
        when(siteRepository.findByUrl(siteEntity.getUrl())).thenReturn(Optional.of(siteEntity));
        when(indexRepository.getLemmasBySiteAndLemmaList(lemmasSet, siteEntityList, maxFreqForSearch)).thenReturn(lemmasInDb);
        when(lemmaRepository.getAvgFreq()).thenReturn(avgFreq);
        when(indexRepository.getMaxAbsRel(lemmasInDb)).thenReturn(maxAbsRelForTest);
        when(indexRepository.getAbsRankFromPage(anyList(), any(PageEntity.class))).thenAnswer(i ->
        {
            Object argument = i.getArgument(1);
            for (PageEntity page : testPagesList)
            {
                if (argument.equals(page))
                {
                    return getMaxAbsRelForPage(page);
                }
            }
            throw new Exception();
        });

        when(indexRepository.getPagesBYLemmaEAndPageEList(any(LemmaEntity.class), anyList())).thenReturn(anyList());
        when(pageRepository.findAllByIdIn(List.of(1, 2, 3))).thenReturn(testPagesList);
        SearchServiceImpl searchService = new SearchServiceImpl(testSitesList, lemmasFinder,
                lemmaRepository, pageRepository, siteRepository, indexRepository);
        SearchResultResponse actualResult = searchService.search(searchQuery, null, 0, 40);
        SearchResultResponse expectedResult = new SearchResultResponse();

        String expectedLemmaSnippet = "<b>" + firstLemma.getLemma() + "</b>";
        String actualSnippet = actualResult.getData().get(0).getSnippet();
        assertTrue(actualSnippet.contains(expectedLemmaSnippet));
    }

    private SiteEntity getSIteEntityForTest()
    {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("TestSite");
        siteEntity.setUrl(FIRST_SITE_URL);
        return siteEntity;
    }

    @SneakyThrows
    private List<PageEntity> getPageEntityList(SiteEntity siteEntity)
    {
        List<PageEntity> testPagesList = new ArrayList<>();
        String pagePath1 = "C:\\final_project_repo\\first_project\\searchengine-master\\src\\test\\resources\\" +
                "datafortests\\secondtestsite\\index.html";
        String pagePath2 = "C:\\final_project_repo\\first_project\\searchengine-master\\src\\test\\resources\\" +
                "datafortests\\secondtestsite\\pages\\page2.html";
        String pagePath3 = "C:\\final_project_repo\\first_project\\searchengine-master\\src\\test\\resources\\" +
                "datafortests\\secondtestsite\\pages\\page3.html";

        List<String> testPagesPaths = List.of(pagePath1, pagePath2, pagePath3);
        int statusCode = 200;
        int i = 1;
        for (String path : testPagesPaths)
        {
            Document document = Jsoup.parse(new File(path));
            String content = document.html();
            PageEntity testPage = new PageEntity();
            testPage.setId(i);
            testPage.setSiteEntity(siteEntity);
            testPage.setCode(statusCode);
            testPage.setContent(content);
            testPage.setPath(path);
            testPagesList.add(testPage);
            i++;
        }
        return testPagesList;
    }

    private float getAvgFreq(List<LemmaEntity> lemmaEntityList)
    {
        return (float) lemmaEntityList.stream().filter(l -> l.getFrequency() > 1)
                        .mapToInt(LemmaEntity::getFrequency).average().getAsDouble();
    }

    private int getMaxAbsRelForTest(List<LemmaEntity> lemmasInDb)
    {
        return indexEntities.stream().filter(i -> lemmasInDb.contains(i.getLemmaEntity()))
                .map(IndexEntity::getRank).max(Comparator.naturalOrder()).get();
    }

    private int getMaxAbsRelForPage(PageEntity page)
    {
        return indexEntities.stream().filter(i -> i.getPageEntity().equals(page)).map(IndexEntity::getRank)
                .reduce(0, Integer::sum);

    }
}
