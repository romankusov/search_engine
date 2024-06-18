package searchengine.dto.search;

import lombok.Data;

import java.util.List;


@Data
public class SearchResultResponse {
    private boolean result;
    private int count;
    private List<SearchData> data;
    private String error;
}
