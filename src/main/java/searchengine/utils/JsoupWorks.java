package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;

@Slf4j
public class JsoupWorks {

    public static Connection.Response getJsoupResponse(String url) throws IOException
    {
        return Jsoup.connect(url).userAgent("Chrome/81.0.4044.138").
                timeout(5 * 1000).
                ignoreHttpErrors(true).
                ignoreContentType(true).
                execute();
    }


    public static PageEntity makeOnePageForDB(SiteEntity siteEntity, String url) throws IOException {
        Connection.Response jsoupResponse = JsoupWorks.getJsoupResponse(url);
        String path = url.substring(siteEntity.getUrl().length() - 1);
        Document document = jsoupResponse.parse();
        try
        {
            int code = jsoupResponse.statusCode();
            String content = document.html();
            return new PageEntity(siteEntity, path, code, content);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            return new PageEntity(siteEntity, path);
        }
    }
}
