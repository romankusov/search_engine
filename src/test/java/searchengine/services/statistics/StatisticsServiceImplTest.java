package searchengine.services.statistics;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.IndexStatus;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsServiceImplTest {

    private final SiteRepository siteRepository = mock(SiteRepository.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final LemmaRepository lemmaRepository = mock(LemmaRepository.class);
    private final SitesList testSitesList = new SitesList();


    @BeforeEach
    @SneakyThrows
    public void setUp()
    {
        Site testSite = new Site();
        testSite.setName("FirstTestSite");
        testSite.setUrl("http://url.com/");
        testSitesList.setSites(List.of(testSite));
    }

    @Test
    @DisplayName("Test of getting statistics")
    public void getStatisticsTest()
    {
        StatisticsServiceImpl service = new StatisticsServiceImpl(testSitesList, pageRepository,
                lemmaRepository, siteRepository);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("FirstTestSite");
        siteEntity.setUrl("http://url.com/");
        siteEntity.setId(1);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(IndexStatus.INDEXED);

        when(siteRepository.findByUrl(siteEntity.getUrl())).thenReturn(Optional.of(siteEntity));
        when(pageRepository.countBySiteEntityName(siteEntity.getName())).thenReturn(1);
        when(lemmaRepository.countBySiteEntityName(siteEntity.getName())).thenReturn(4);
        StatisticsResponse statisticsResponse = service.getStatistics();

        TotalStatistics expectedTotalStatistics = statisticsResponse.getStatistics().getTotal();
        int expectedSitesNum = expectedTotalStatistics.getSites();
        int expectedPagesNum = expectedTotalStatistics.getPages();
        int expectedLemmasNum = expectedTotalStatistics.getLemmas();

        assertEquals(expectedSitesNum, 1);
        assertEquals(expectedPagesNum, 1);
        assertEquals(expectedLemmasNum, 4);
    }

    @Test
    @DisplayName("Test error while getting statistics")
    public void getStatisticsTest_error()
    {
        StatisticsServiceImpl service = new StatisticsServiceImpl(testSitesList, pageRepository,
                lemmaRepository, siteRepository);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setUrl("http://url.com/");

        when(siteRepository.findByUrl(anyString())).thenThrow(NullPointerException.class);

        StatisticsResponse expectedResponse = new StatisticsResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Указанная страница не найдена");

        StatisticsResponse actualResponse = service.getStatistics();
        assertEquals(expectedResponse, actualResponse);
    }
}
