package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexStatus;
import searchengine.model.SiteEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    Optional<SiteEntity> findByUrl(String url);

    List<SiteEntity> findAll();


    @Transactional
    default void update(SiteEntity siteEntity, IndexStatus status, String error)
    {
        String url = siteEntity.getUrl();
        SiteEntity sEforUpdate = findByUrl(url).get();
        sEforUpdate.setStatus(status);
        sEforUpdate.setLastError(error);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }

    @Transactional
    default void update(SiteEntity siteEntity, String error)
    {
        String url = siteEntity.getUrl();
        SiteEntity sEforUpdate = findByUrl(url).get();
        sEforUpdate.setLastError(error);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }

    @Transactional
    default void update(SiteEntity siteEntity, IndexStatus status)
    {
        String url = siteEntity.getUrl();
        SiteEntity sEforUpdate = findByUrl(url).get();
        sEforUpdate.setStatus(status);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }




}
