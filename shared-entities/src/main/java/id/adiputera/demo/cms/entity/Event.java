package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsSearchable;
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
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsSearchable(name = "title", displayName = "Title", type = "string")
@CmsSearchable(name = "location", displayName = "Location", type = "string")
@CmsSearchable(name = "slug", displayName = "Slug", type = "string")
public class Event extends CatalogAwareModel {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String location;

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
        return new ItemSearchResultDTO(getId() != null ? String.valueOf(getId()) : null, getTitle(), getLocation());
    }
}
