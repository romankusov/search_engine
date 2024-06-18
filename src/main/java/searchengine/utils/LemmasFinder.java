package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class LemmasFinder {

    private final String REG_EXP_RUSSIAN_WORD = "([а-яА-Я]+(-[а-яА-Я]+)*)";

    private final LuceneMorphology luceneMorph;

    public static LemmasFinder getInstance() throws IOException {
        LuceneMorphology morphology= new RussianLuceneMorphology();
        return new LemmasFinder(morphology);
    }
    public LemmasFinder(LuceneMorphology luceneMorph) {
        this.luceneMorph = luceneMorph;
    }

    public LinkedHashMap<String, Integer> getLemmaMap(String text) throws Exception {//Linked?
        List<String> russianPropperWordsList = russianPropperWordsList(text);
        if(russianPropperWordsList.isEmpty())
        {
            throw new Exception("На странице отсутствуют русские слова");
        }
        LinkedHashMap<String, Integer> lemmaMap = new LinkedHashMap<>();
        for (String word : russianPropperWordsList)
        {
            if(!luceneMorph.checkString(word))
            {
                continue;
            }
            String lemma = luceneMorph.getNormalForms(word).get(0);
            lemmaMap.put(lemma, lemmaMap.getOrDefault(lemma, 0) + 1);
        }
        return lemmaMap;
    }

    public List<String> russianPropperWordsList(String text)
    {
        List<String> stringListFromText = getStringListFromText(text);

        return stringListFromText.stream().filter(this::isPropperWord).collect(Collectors.toList());
    }

    public List<String> getStringListFromText(String text)
    {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).trim().split("\\b"))
                .filter(s -> s.matches(REG_EXP_RUSSIAN_WORD)).toList();
    }

    public String getLemma(String word)
    {
        return luceneMorph.getNormalForms(word).get(0);
    }

    private boolean isPropperWord(String word)
    {
        String[] morphElementsInfo = luceneMorph.getMorphInfo(word).get(0).split("\\s+");
        return morphElementsInfo.length > 2;
    }
}
