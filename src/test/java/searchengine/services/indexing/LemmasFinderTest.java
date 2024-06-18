package searchengine.services.indexing;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.utils.LemmasFinder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LemmasFinderTest {

    private LemmasFinder lemmasFinder ;


    public LemmasFinderTest() throws IOException {
        lemmasFinder = LemmasFinder.getInstance();
    }


    @Test
    @SneakyThrows
    @DisplayName("Test of proper lemmatization")
    public void getLemmaMapTest()
    {
        String text = "Один купец ехал, ехал по дороге, дороге, дороге на ярмарку и вез в телеге мешок " +
                "картошки, мешок яблок, мешок лука";
        Map<String, Integer> expectedMap = new LinkedHashMap<>();
        expectedMap.put("один", 1);
        expectedMap.put("купец", 1);
        expectedMap.put("ехать", 2);
        expectedMap.put("дорога", 3);
        expectedMap.put("ярмарка", 1);
        expectedMap.put("везти", 1);
        expectedMap.put("телега", 1);
        expectedMap.put("мешок", 3);
        expectedMap.put("картошка", 1);
        expectedMap.put("яблоко", 1);
        expectedMap.put("лук", 1);
        Map<String, Integer> actualMap = lemmasFinder.getLemmaMap(text);
        assertEquals(expectedMap, actualMap);
    }
}
