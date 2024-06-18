package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResultResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.LemmasFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchServiceImpl implements SearchService{

    private final SitesList sites;

    @Autowired
    private LemmasFinder lemmasFinder;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private IndexRepository indexRepository;

    List<LemmaEntity> lemmasInDb = new ArrayList<>();

    private int maxAbsRel = 0;

    public SearchServiceImpl(SitesList sites, LemmasFinder lemmasFinder, LemmaRepository lemmaRepository,
                             PageRepository pageRepository, SiteRepository siteRepository,
                             IndexRepository indexRepository) {
        this.sites = sites;
        this.lemmasFinder = lemmasFinder;
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public SearchResultResponse search(String query, String siteString, int offset, int limit)
    {
        SearchResultResponse searchResultResponse = new SearchResultResponse();
        try
        {
            Map<String, Integer> lemmaMap = lemmasFinder.getLemmaMap(query);
            Set<String> lemmasFromQuery = lemmaMap.keySet();
            List<SiteEntity> siteEntities = new ArrayList<>();
            if (siteString == null)
            {
                siteEntities =  siteRepository.findAll();
            }
            if (sites.getSites().stream().anyMatch(s -> s.getUrl().equals(siteString)))
            {
                siteEntities.add(siteRepository.findByUrl(siteString).get());
            }
            int maxFreqForSearch = Math.round(lemmaRepository.getAvgFreq());
            List<SearchData> searchDataList = new ArrayList<>();
            lemmasInDb = indexRepository.getLemmasBySiteAndLemmaList(lemmasFromQuery, siteEntities, maxFreqForSearch);
            maxAbsRel = indexRepository.getMaxAbsRel(lemmasInDb);
            List<Integer> initialPagesIds = indexRepository.getPageEntityByLemma(lemmasInDb.get(0));
            List<PageEntity> pagesForSearch = getPagesForSearch(initialPagesIds);
            if (!pagesForSearch.isEmpty())
            {
                pagesForSearch.forEach(p -> {
                    try {
                        searchDataList.add(getSearchDataFromPageE(p, lemmasFromQuery));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            searchResultResponse.setData(searchDataList);
            int count = searchResultResponse.getData().size();
            searchResultResponse.setCount(count);
            searchResultResponse.setResult(true);
            return searchResultResponse;
        } catch (Exception ex)
        {
            searchResultResponse.setResult(false);
            searchResultResponse.setError("Задан пустой (некорректный) поисковый запрос " +
                    "либо слова из запроса отсутствуют в базе данных");
            return searchResultResponse;
        }
    }

    private List<PageEntity> getPagesForSearch(List<Integer> initialPagesIdsList)
    {
        List<Integer> pageIdsForSearch = initialPagesIdsList;
        for (int i = 1; i < lemmasInDb.size(); i++)
        {
            LemmaEntity lemmaE = lemmasInDb.get(i);
            List<Integer> pageIdsByLemma = indexRepository.getPagesBYLemmaEAndPageEList(lemmaE, initialPagesIdsList);
            pageIdsForSearch = initialPagesIdsList.stream().filter(pageIdsByLemma::contains)
                    .collect(Collectors.toList());
        }
        return pageRepository.findAllByIdIn(pageIdsForSearch);
    }


    private SearchData getSearchDataFromPageE(PageEntity page, Set<String> lemmasFromQuery) throws Exception
    {
        String siteUrl = page.getSiteEntity().getUrl();
        String uri = page.getPath();
        String htmlContent = page.getContent();
        String title = Jsoup.parse(htmlContent).title();

        String siteName = sites.getSites().stream().filter(s -> s.getUrl().equals(siteUrl))
                .map(Site::getName).findFirst().get();
        String cleanTextFromPage = Jsoup.clean(htmlContent, Safelist.none());
        String snippet = getSeparateSnippet(lemmasFromQuery, cleanTextFromPage);
        float relevance = getRelevance(page);

        SearchData searchData = new SearchData();
        searchData.setSite(siteUrl);
        searchData.setSiteName(siteName);
        searchData.setUri(uri);
        searchData.setTitle(title);
        searchData.setSnippet(snippet);
        searchData.setRelevance(relevance);

        return searchData;
    }

    private String getSeparateSnippet(Set<String> lemmasFromQuery, String cleanTextFromPage)
    {
        List<String> cleanTextStringList = lemmasFinder.getStringListFromText(cleanTextFromPage);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, Integer> coincidence = new TreeMap<>();
        for (String word : cleanTextStringList)
        {
            String lemmaInText = lemmasFinder.getLemma(word);
            if (lemmasFromQuery.contains(lemmaInText))
            {
                int numberInList = cleanTextStringList.indexOf(word);
                coincidence.put(lemmaInText, numberInList);
            }
        }

        List<String> lemmasFromMap = new ArrayList<>(coincidence.keySet());

        for (int i = 0; i < lemmasFromMap.size(); i++)
        {
            int numberInTextCurrent = coincidence.get(lemmasFromMap.get(i));
            int numberInTextPrevious = 0;
            if (i > 0)
            {
                numberInTextPrevious = coincidence.get(lemmasFromMap.get(i - 1));
            }
            int beginSnippetPart = Math.max(numberInTextCurrent - 8, 0);
            int endSnippetPart = Math.min(numberInTextCurrent + 8, cleanTextStringList.size() - 1);
            if (numberInTextPrevious > beginSnippetPart)
            {
                continue;
            }
            for (int j = beginSnippetPart; j <= endSnippetPart; j++)
            {
                String wordToAppend = "";
                if (coincidence.containsValue(j))
                {
                    wordToAppend = "<b>"  + cleanTextStringList.get(j) + "</b> ";
                } else
                {
                    wordToAppend = cleanTextStringList.get(j) + " ";
                }
                stringBuilder.append(wordToAppend);
            }
        }
        return stringBuilder.toString();
    }

    private float getRelevance(PageEntity page)
    {
        int maxAbsRelFromPage = indexRepository.getAbsRankFromPage(lemmasInDb, page);
        return (float) maxAbsRelFromPage/maxAbsRel;
    }
}
