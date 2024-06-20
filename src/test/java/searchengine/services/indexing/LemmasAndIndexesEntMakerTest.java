package searchengine.services.indexing;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.LemmasAndIndexesEntMaker;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LemmasAndIndexesEntMakerTest {

    LemmasAndIndexesEntMaker testLemmasAndIndexesEntMaker = new LemmasAndIndexesEntMaker(new LinkedHashMap<LemmaEntity, Integer>(),
            new ConcurrentLinkedQueue<>());
    private final PageEntity testPage = new PageEntity();
    private final Map<LemmaEntity, Integer> expectedLemmaMapForDB = new LinkedHashMap<>();
    private final ConcurrentLinkedQueue<IndexEntity> expectedIndexEntityQueue = new ConcurrentLinkedQueue<>();

    LemmasAndIndexesEntMakerTest() throws Exception {
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("TestSite");
        siteEntity.setUrl("Url");
        String path = "C:\\final_project_repo\\first_project\\searchengine-master\\src\\test\\resources\\" +
                "datafortests\\firsttestsite\\pages\\page3.html";
        Document document = Jsoup.parse(new File(path));
        String content = document.html();
        int statusCode = 200;
        testPage.setSiteEntity(siteEntity);
        testPage.setCode(statusCode);
        testPage.setContent(content);
        testPage.setId(1);

        LemmaEntity lemmaEntity1 = new LemmaEntity(siteEntity, "сено", 1);
        LemmaEntity lemmaEntity2 = new LemmaEntity(siteEntity, "твид", 1);
        LemmaEntity lemmaEntity3 = new LemmaEntity(siteEntity, "ультрафиолет", 1);
        LemmaEntity lemmaEntity4 = new LemmaEntity(siteEntity, "филин", 1);

        expectedLemmaMapForDB.put(lemmaEntity1, 1);
        expectedLemmaMapForDB.put(lemmaEntity2, 1);
        expectedLemmaMapForDB.put(lemmaEntity3, 1);
        expectedLemmaMapForDB.put(lemmaEntity4, 1);

        IndexEntity indexEntity1 = new IndexEntity(testPage, lemmaEntity1, 20);
        IndexEntity indexEntity2 = new IndexEntity(testPage, lemmaEntity2, 16);
        IndexEntity indexEntity3 = new IndexEntity(testPage, lemmaEntity3, 18);
        IndexEntity indexEntity4 = new IndexEntity(testPage, lemmaEntity4, 24);

        expectedIndexEntityQueue.add(indexEntity1);
        expectedIndexEntityQueue.add(indexEntity2);
        expectedIndexEntityQueue.add(indexEntity3);
        expectedIndexEntityQueue.add(indexEntity4);

        testLemmasAndIndexesEntMaker.addLeAndIeToTempCollection(testPage);
    }

    @Test
    @SneakyThrows
    @DisplayName("Test adding indexes and lemmas to temp collection")
    void addLeAndIeToTempCollection() {
        Map<LemmaEntity, Integer> actualMap = testLemmasAndIndexesEntMaker.getLemmaMapForDb();

        assertEquals(expectedLemmaMapForDB, actualMap);
    }

    @Test
    @DisplayName("Test getting lemmas for save to DB")
    void getLemmasListForSave() {
        Set<LemmaEntity> expectedLemmaSet = expectedLemmaMapForDB.keySet();
        List<LemmaEntity> expectedLemmaList = new ArrayList<>(expectedLemmaSet);
        Set<LemmaEntity> actualLemmaSet = testLemmasAndIndexesEntMaker.getLemmaMapForDb().keySet();
        List<LemmaEntity> actualLemmaList = new ArrayList<>(actualLemmaSet);

        assertEquals(expectedLemmaList, actualLemmaList);
    }

    @Test
    @SneakyThrows
    @DisplayName("Test getting indexes for save to DB")
    void getIndexEntityQueueForSave() {
        ConcurrentLinkedQueue<IndexEntity> actualQueue = testLemmasAndIndexesEntMaker.getIndexEntityQueue();
        List<IndexEntity> expectedIndexes = new ArrayList<>(actualQueue);
        List<IndexEntity> actualIndexes = new ArrayList<>(expectedIndexEntityQueue);

        assertEquals(expectedIndexes, actualIndexes);
    }
}