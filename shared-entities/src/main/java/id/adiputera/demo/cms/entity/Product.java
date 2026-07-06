package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Product class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "products", 
    indexes = {
        @Index(name = "idx_products_code", columnList = "code")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code", "catalog_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends CatalogAwareModel {

    @CmsField(displayName = "Product Code", searchable = true, order = 1)
    @NotBlank(message = "Product code is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String code;

    @CmsField(displayName = "Product Name", searchable = true, order = 2)
    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;

    @CmsField(displayName = "Price", type = "number", searchable = true, order = 3)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Gets the synchronization key for catalog aware model.
     *
     * @see CatalogAwareModel#getSyncKey()
     * @return The product code as sync key.
     */
    @Override
    public String getSyncKey() {
        return this.code;
    }

    /**
     * Converts the entity to an item search result DTO.
     *
     * @see CatalogAwareModel#toItemSearchResultDTO()
     * @return The item search result DTO.
     */
    @Override
    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(getCode(), getName(), getCode() + " - $" + getPrice());
    }
}
