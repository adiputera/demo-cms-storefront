package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalog class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(
    name = "catalogs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"catalog_id", "version"})
    },
    indexes = {
        @Index(name = "idx_catalog_id_version", columnList = "catalog_id, version")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog extends ItemModel {

    @NotBlank(message = "Catalog ID is required")
    @Column(name = "catalog_id", nullable = false, length = 100)
    private String catalogId;

    @NotNull(message = "Version is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "version", nullable = false, length = 20)
    private CatalogVersion version;

    @Override
    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(
                getId() != null ? String.valueOf(getId()) : null,
                catalogId,
                version != null ? version.name() : null
        );
    }
}
