package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Article class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article extends CatalogAwareModel {

    @CmsField(displayName = "Title", searchable = true, order = 1)
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @CmsField(displayName = "Slug", searchable = true, order = 2)
    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String slug;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "uid", nullable = false, updatable = false)
    private String uid;

    @PrePersist
    protected void initUid() {
        if (uid == null) {
            uid = UUID.randomUUID().toString();
        }
    }

    @Override
    public String getSyncKey() {
        return uid;
    }

    @Override
    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(getId() != null ? String.valueOf(getId()) : null, getTitle(), "Article");
    }
}
