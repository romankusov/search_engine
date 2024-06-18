package searchengine.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResultResponse;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
public class ApiControllerTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    IndexingService indexingService;

    @MockBean
    SearchService searchService;

    @MockBean
    StatisticsService statisticsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @SneakyThrows
    @DisplayName("Successful GET /statistics")
    void testOkGetStatistics() {
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setStatistics(new StatisticsData());
        statisticsResponse.setResult(true);

        when(statisticsService.getStatistics()).thenReturn(statisticsResponse);

        mockMvc.perform(
                get("/api/statistics")
        ).andExpectAll(
                status().isOk(),
                content().json(objectMapper.writeValueAsString(statisticsResponse))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Not found GET /statistics")
    void testBadRequestGetStatistics() {
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setResult(false);
        statisticsResponse.setError("Указанная страница не найдена");

        when(statisticsService.getStatistics()).thenReturn(statisticsResponse);

        mockMvc.perform(
                get("/api/statistics")
        ).andExpectAll(
                status().isNotFound(),
                content().string(statisticsResponse.getError())
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Successful GET /startIndexing")
    void testOkGetStartIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(true);

        when(indexingService.startIndexing()).thenReturn(indexingResponse);

        mockMvc.perform(
                get("/api/startIndexing")
        ).andExpectAll(
                status().isOk(),
                content().json(objectMapper.writeValueAsString(indexingResponse))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Bad request GET /startIndexing")
    void testBadRequestGetStartIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(false);
        indexingResponse.setError("Индексация уже запущена");

        when(indexingService.startIndexing()).thenReturn(indexingResponse);

        mockMvc.perform(
                get("/api/startIndexing")
        ).andExpectAll(
                status().isBadRequest(),
                content().string(indexingResponse.getError())
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Successful GET /stopIndexing")
    void testOkStopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(true);

        when(indexingService.stopIndexing()).thenReturn(indexingResponse);

        mockMvc.perform(
                get("/api/stopIndexing")
        ).andExpectAll(
                status().isOk(),
                content().json(objectMapper.writeValueAsString(indexingResponse))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Bad request GET /stopIndexing")
    void testBadRequestStopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(false);
        indexingResponse.setError("Индексация не запущена");

        when(indexingService.startIndexing()).thenReturn(indexingResponse);

        mockMvc.perform(
                get("/api/startIndexing")
        ).andExpectAll(
                status().isBadRequest(),
                content().string(indexingResponse.getError())
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Successfull POST /indexPage")
    void testOkIndexPage() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(true);

        when(indexingService.indexPage(anyString())).thenReturn(indexingResponse);

        mockMvc.perform(
                post("/api/indexPage").param("url", "test.url")
        ).andExpectAll(
                status().isOk(),
                content().json(objectMapper.writeValueAsString(indexingResponse))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Bad request POST /indexPage")
    void testBadRequestIndexPage() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(false);
        indexingResponse.setError("Данная страница находится за пределами сайтов, " +
                "указанных в конфигурационном файле");

        when(indexingService.indexPage(anyString())).thenReturn(indexingResponse);

        mockMvc.perform(
                post("/api/indexPage").param("url", "test.url")
        ).andExpectAll(
                status().isBadRequest(),
                content().string(indexingResponse.getError())
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Successful GET /search")
    void testOkSearch() {
        SearchResultResponse searchResultResponse = new SearchResultResponse();
        searchResultResponse.setResult(true);

        when(searchService.search(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(searchResultResponse);

        mockMvc.perform(
                get("/api/search")
                        .param("query", "testQuery")
                        .param("site", "site")
                        .param("offset", "0")
                        .param("limit", "20")
        ).andExpectAll(
                status().isOk(),
                content().json(objectMapper.writeValueAsString(searchResultResponse))
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Bad request GET /search")
    void testBadRequestSearch() {
        SearchResultResponse searchResultResponse = new SearchResultResponse();
        searchResultResponse.setResult(false);
        searchResultResponse.setError("Задан пустой (некорректный) поисковый запрос, " +
                "либо слова из запроса отсутствуют в базе данных");

        when(searchService.search(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(searchResultResponse);

        mockMvc.perform(
                get("/api/search")
                        .param("query", "testQuery")
                        .param("site", "site")
                        .param("offset", "0")
                        .param("limit", "20")
        ).andExpectAll(
                status().isBadRequest(),
                content().string(searchResultResponse.getError())
        );
    }
}
