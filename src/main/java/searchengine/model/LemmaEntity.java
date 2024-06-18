package searchengine.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lemmas")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    public LemmaEntity(SiteEntity siteEntity, String lemma) {
        this.siteEntity = siteEntity;
        this.lemma = lemma;
    }

    public LemmaEntity(SiteEntity siteEntity, String lemma, int frequency) {
        this.siteEntity = siteEntity;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LemmaEntity that)) return false;
        return siteEntity.getUrl().equals(that.siteEntity.getUrl()) && lemma.equals(that.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siteEntity, lemma);
    }

}
