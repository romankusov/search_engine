package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Set;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

    @Transactional
    void deleteByPageEntity(PageEntity page);

    @Transactional
    @Query("SELECT " +
            "sum(i.rank) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity IN (:lemmaEntitySet) AND i.pageEntity = :page")
    int getAbsRankFromPage(@Param("lemmaEntitySet") List<LemmaEntity> lemmaEntitySet,
                              @Param("page") PageEntity page);

    @Transactional
    @Query("SELECT i.lemmaEntity " +
            "FROM IndexEntity i " +
            "WHERE i.pageEntity = :page")
    List<LemmaEntity> getLemmasByPage(@Param("page") PageEntity pageEntity);

    @Transactional
    @Query("SELECT i.pageEntity.id " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemmaEntity")
    List<Integer> getPageEntityByLemma(@Param("lemmaEntity") LemmaEntity lemmaEntity);

    @Transactional
    @Query("SELECT MAX(i.rank) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity " +
            "IN (:lemmaEntityList)")
    Integer getMaxAbsRel(@Param("lemmaEntityList") List<LemmaEntity> lemmaEntityList);


    @Transactional// Better?
    @Query("SELECT i.lemmaEntity " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity.lemma IN (:lemmaStringSet) " +
            "AND i.lemmaEntity.siteEntity IN (:siteEntities) " +
            "AND i.lemmaEntity.frequency < :maxFreqForSearch " +
            "ORDER BY i.lemmaEntity.frequency")
    List<LemmaEntity> getLemmasBySiteAndLemmaList(@Param("lemmaStringSet") Set<String> lemmaStringSet,
                                                  @Param("siteEntities") List<SiteEntity> siteEntities,
                                                  @Param("maxFreqForSearch") int maxFreqForSearch);

    @Query("SELECT i.pageEntity.id " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemmaEntityFromQuery " +
            "AND i.pageEntity.id IN (:pageEntityList)")
    List<Integer> getPagesBYLemmaEAndPageEList(@Param("lemmaEntityFromQuery") LemmaEntity lemmaEntityFromQuery,
                                                  @Param("pageEntityList") List<Integer> pageEntityIdList);

    @Query("SELECT SUM(i.lemmaEntity.frequency) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity IN (:lemmaEntityListFromDB)")
    Integer getSumOfFreqFromLemmas(@Param("lemmaEntityListFromDB") List<LemmaEntity> lemmaEntityListFromDB);
}
