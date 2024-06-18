package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    @Transactional
    Optional<PageEntity> findByPathAndSiteEntityUrl(String path, String url);

    List<PageEntity> findAllByIdIn(List <Integer> ids);

    int countBySiteEntityName(String siteEntityName);
}
