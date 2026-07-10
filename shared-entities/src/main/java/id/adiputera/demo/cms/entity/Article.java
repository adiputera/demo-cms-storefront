package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @CmsField(
        displayName = "Title",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 1
    )
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @CmsField(
        displayName = "Slug",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 2
    )
    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String slug;

    @Column(name = "body", columnDefinition = "TEXT")
    @CmsField(
        displayName = "Body",
        type = CmsFieldType.TEXT,
        showAsColumn = false
    )
    private String body;

    @Override
    public String getSyncKey() {
        return slug;
    }

    @Override
    public String getSyncKeyFieldName() {
        return "slug";
    }

    @Override
    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(getSyncKey(), getTitle(), "Article");
    }
}
