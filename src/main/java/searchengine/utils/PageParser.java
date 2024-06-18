package searchengine.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class PageParser extends RecursiveTask <LemmasAndIndexesEntMaker> {
    private final String url;
    private final SiteEntity site;
    private final PageRepository pageRepository;
    private final Set<String> uniqueLinksSet;
    private SiteRepository siteRepository;
    private LemmasAndIndexesEntMaker lemmasAndIndexesEntMaker;

    @Override
    protected LemmasAndIndexesEntMaker compute()
    {
        uniqueLinksSet.add(url);
        try {
            List<PageParser> tasklist = new ArrayList<>();
            if (SiteParser.isStop())
            {
                tasklist.clear();
                return new LemmasAndIndexesEntMaker();
            }
            Connection.Response jsoupResponse = JsoupWorks.getJsoupResponse(url);
            Document document = jsoupResponse.parse();

            Set <String> setOfLinks = new HashSet<>();
            if (Objects.requireNonNull(jsoupResponse.contentType()).startsWith("text"))
            {
                PageEntity page = pageRepository.saveAndFlush(makePageForDB(jsoupResponse, document));
                if (page.getCode() == 200)
                {
                    lemmasAndIndexesEntMaker.addLeAndIeToTempCollection(page);
                    setOfLinks = getLinksFromPage(document);
                }
            }
            if (!setOfLinks.isEmpty())
            {
                uniqueLinksSet.addAll(setOfLinks);
                for (String link: setOfLinks)
                {
                    Thread.sleep(200);

                    PageParser task = new PageParser(link, site, pageRepository,
                            uniqueLinksSet, siteRepository, lemmasAndIndexesEntMaker);
                    task.fork();
                    tasklist.add(task);
                }

            }
            tasklist.forEach(ForkJoinTask::join);
        } catch (IOException e)
        {
            e.printStackTrace();
            String errorMsg = e.getLocalizedMessage() + " on page: " + url;
            siteRepository.update(site, errorMsg);
        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage() + " on page: " + url;
            siteRepository.update(site, errorMsg);
            e.printStackTrace();
        }
        return lemmasAndIndexesEntMaker;
    }

    private boolean isPropperLink(String url)
    {
        return  url.startsWith(site.getUrl()) &&
                !url.contains("?") &&
                !url.contains("#") &&
                !url.endsWith("jpeg/") &&
                !url.endsWith("jpg/") &&
                !url.endsWith("pdf/") &&
                !url.endsWith("mp3/");
    }
    private String linkFormat(String link)
    {
        String formatedLink = link;
        if (!link.endsWith("/"))
        {
            formatedLink = link + "/";
        }
        return formatedLink;
    }

    private PageEntity makePageForDB(Connection.Response jsoupResponse, Document document) throws IOException {
        String path = url.substring(site.getUrl().length() - 1);
        int code = jsoupResponse.statusCode();
        if (code != 200)
        {
            return new PageEntity(site, path, code, "");
        }
        String content = document.html();
        return new PageEntity(site, path, code, content);
    }

    private Set<String> getLinksFromPage(Document document) throws IOException
    {
        Elements elements = document.select("a");
        return elements.stream().map(e -> e.absUrl("href"))
                .map(String::toLowerCase)
                .map(this::linkFormat)
                .filter(this::isPropperLink)
                .filter(e -> !uniqueLinksSet.contains(e))
                .collect(Collectors.toSet());
    }
}
