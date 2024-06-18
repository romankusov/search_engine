package searchengine.services.indexing;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing() throws InterruptedException;
    IndexingResponse stopIndexing() throws InterruptedException;
    IndexingResponse indexPage(String url) throws Exception;
}
