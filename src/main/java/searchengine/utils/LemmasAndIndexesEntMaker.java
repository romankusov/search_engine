package searchengine.utils;

import lombok.NoArgsConstructor;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@NoArgsConstructor
public class LemmasAndIndexesEntMaker {

    private static LemmasFinder lemmasFinder;

    static {
        try {
            lemmasFinder = LemmasFinder.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<LemmaEntity, Integer> lemmaMapForDB;
    private ConcurrentLinkedQueue<IndexEntity> indexEntityQueue;

    public LemmasAndIndexesEntMaker(Map<LemmaEntity, Integer> lemmaMapForDB,
                                    ConcurrentLinkedQueue<IndexEntity> indexEntityQueue) throws Exception {
        this.lemmaMapForDB = lemmaMapForDB;
        this.indexEntityQueue = indexEntityQueue;
    }


    public void addLeAndIeToTempCollection(PageEntity page) throws Exception
    {
        Map<String, Integer> lemmasAndRankFromPage = getLemmasAndRankFromPage(page);
        SiteEntity siteEntity = page.getSiteEntity();

        for (String strLemma : lemmasAndRankFromPage.keySet())
        {
            LemmaEntity lemmaEntity = new LemmaEntity(siteEntity, strLemma);
            putNewLemmas(lemmaEntity);
            Integer rank = lemmasAndRankFromPage.get(strLemma);
            indexEntityQueue.add(new IndexEntity(page, lemmaEntity, rank));
        }
    }

    public List<LemmaEntity> getLemmasListForSave()
    {
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        for (LemmaEntity lemmaEntity : lemmaMapForDB.keySet())
        {
            int frequency = lemmaMapForDB.get(lemmaEntity);
            lemmaEntity.setFrequency(frequency);
            lemmaEntityList.add(lemmaEntity);
        }

        return lemmaEntityList;
    }

    public ConcurrentLinkedQueue<IndexEntity> getIndexEntityQueueForSave(List<LemmaEntity> savedLemmaEntityList)
    {
        for (LemmaEntity lemmaEntity : savedLemmaEntityList)
        {
            for (IndexEntity indexEntity : indexEntityQueue)
            {
                boolean sameLemma = lemmaEntity.getLemma().equals(indexEntity.getLemmaEntity().getLemma())
                        && lemmaEntity.getSiteEntity().equals(indexEntity.getLemmaEntity().getSiteEntity());
                if (sameLemma)
                {
                    indexEntity.setLemmaEntity(lemmaEntity);
                }
            }
        }
        return indexEntityQueue;
    }

    public ConcurrentLinkedQueue<IndexEntity> getIndexEntityQueue()
    {
        return indexEntityQueue;
    }

    public void clear()
    {
        indexEntityQueue.clear();
        lemmaMapForDB.clear();
    }

    public Map<LemmaEntity, Integer> getLemmaMapForDb()
    {
        return lemmaMapForDB;
    }

    private Map<String, Integer> getLemmasAndRankFromPage(PageEntity page) throws Exception
    {
        String text = page.getContent();
        return lemmasFinder.getLemmaMap(text);
    }

    private void putNewLemmas(LemmaEntity lemmaEntity)
    {
        int frequency = lemmaMapForDB.getOrDefault(lemmaEntity, 0) + 1;
        lemmaMapForDB.put(lemmaEntity, frequency);
    }
}
