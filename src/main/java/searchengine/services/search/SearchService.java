package searchengine.services.search;

import searchengine.dto.search.SearchResultResponse;

public interface SearchService {
    SearchResultResponse search(String query, String site, int offset, int limit);
}
