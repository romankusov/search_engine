package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.ConstraintMode.CONSTRAINT;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "pages", indexes = {@Index(name = "path_index", columnList = "path" )})
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne//(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id",foreignKey = @ForeignKey(value = CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE CASCADE ON UPDATE CASCADE"), nullable = false)
    private SiteEntity siteEntity;

    @Column(columnDefinition = "varchar(200) NOT NULL")
    private String path;

    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public PageEntity(SiteEntity siteEntity, String path, int code, String content) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    public PageEntity(SiteEntity siteEntity, String path) {
        this.siteEntity = siteEntity;
        this.path = path;
    }
}
