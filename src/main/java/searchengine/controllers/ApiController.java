package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResultResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService)
    {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> statistics()
    {
        StatisticsResponse statisticsResponse = statisticsService.getStatistics();
        if (statisticsResponse.isResult())
        {
            return ResponseEntity.ok(statisticsResponse);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(statisticsResponse.getError());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() throws InterruptedException
    {
        IndexingResponse indexingResponse = indexingService.startIndexing();
        if (indexingResponse.isResult())
        {
            return ResponseEntity.ok(indexingResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(indexingResponse.getError());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() throws InterruptedException
    {
        IndexingResponse indexingResponse = indexingService.stopIndexing();
        if (indexingResponse.isResult())
        {
            return ResponseEntity.ok(indexingResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(indexingResponse.getError());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url) throws Exception {
        IndexingResponse indexingResponse = indexingService.indexPage(url);
        if (indexingResponse.isResult())
        {
            return ResponseEntity.ok(indexingResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(indexingResponse.getError());

    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query,
                                    @RequestParam(required = false) String site,
                                    @RequestParam(defaultValue = "0") int offset,
                                    @RequestParam(defaultValue = "20") int limit)
    {
        SearchResultResponse searchResultResponse = searchService.search(query, site, offset, limit);
        if (searchResultResponse.isResult())
        {
            return ResponseEntity.ok(searchResultResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(searchResultResponse.getError());
    }

}
