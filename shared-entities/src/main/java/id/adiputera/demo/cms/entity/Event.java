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
 * Event class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends CatalogAwareModel {

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

    @Column(name = "description", columnDefinition = "TEXT")
    @CmsField(
        displayName = "Description",
        type = CmsFieldType.TEXT,
        showAsColumn = false
    )
    private String description;

    @CmsField(
        displayName = "Location",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 3
    )
    @NotBlank(message = "Location is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String location;

    @CmsField(
        displayName = "Event Date",
        type = CmsFieldType.DATETIME,
        required = true,
        order = 4
    )
    @Column(name = "event_date", nullable = false)
    private java.time.LocalDateTime eventDate;

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
        return new ItemSearchResultDTO(getSyncKey(), getTitle(), getLocation());
    }
}
