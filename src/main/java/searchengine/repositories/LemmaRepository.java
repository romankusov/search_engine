package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    List<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);
    List<LemmaEntity> findAllByLemma(String lemma);
    LemmaEntity findByLemmaAndSiteEntity(String lemma, SiteEntity siteEntity);

    int countBySiteEntityName(String siteEntityName);

    @Transactional
    default void decreaseFrequencyLemmaAndGetZeroFr(List<LemmaEntity> lemmasForDecrease)
    {
        lemmasForDecrease.forEach(l -> l.setFrequency(l.getFrequency() - 1));
        saveAll(lemmasForDecrease);
        deleteZeroFreqLemmas();
    }

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lemmas l WHERE l.frequency = 0", nativeQuery = true)
    void deleteZeroFreqLemmas();

    @Transactional
    default List<LemmaEntity> upsertLemmas(SiteEntity siteEntity, List<LemmaEntity> newLemmas)
    {
        List<LemmaEntity> lemmaEntityListForIndex = new ArrayList<>();
        List<LemmaEntity> lemmasBySiteList = findBySiteEntity(siteEntity);
        Map<Boolean, List<LemmaEntity>> lemmaMapForUpsert = newLemmas.stream()
                .collect(Collectors.partitioningBy(lemmasBySiteList::contains));
        for (Map.Entry <Boolean, List<LemmaEntity>> entry : lemmaMapForUpsert.entrySet())
        {
            if (!entry.getKey())
            {
                lemmaEntityListForIndex.addAll(saveAll(entry.getValue()));
                continue;
            }
            lemmasBySiteList.stream().filter(l -> entry.getValue().contains(l))
                    .forEach(l -> l.setFrequency(l.getFrequency() + 1));
            lemmaEntityListForIndex.addAll(saveAll(lemmasBySiteList));
        }
        return lemmaEntityListForIndex;
    }

    @Query("SELECT AVG(l.frequency) FROM LemmaEntity l WHERE l.frequency > 1")
    float getAvgFreq();


}
