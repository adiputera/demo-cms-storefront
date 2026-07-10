package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.converter.CurrencyFormatter;
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
 
     @CmsField(
         displayName = "Product Code",
         type = CmsFieldType.STRING,
         required = true,
         editableOnUpdate = false,
         searchable = true,
         order = 1
     )
     @NotBlank(message = "Product code is required")
     @Size(max = 100)
     @Column(nullable = false)
     private String code;
 
     @CmsField(
         displayName = "Product Name",
         type = CmsFieldType.STRING,
         required = true,
         searchable = true,
         order = 2
     )
     @NotBlank(message = "Product name is required")
     @Size(max = 255)
     @Column(nullable = false)
     private String name;
 
     @CmsField(
         displayName = "Product Image",
         type = CmsFieldType.IMAGE,
         required = false,
         order = 3
     )
     @Size(max = 500)
     @Column(name = "image_url")
     private String imageUrl;
 
     @CmsField(
         displayName = "Price",
         type = CmsFieldType.NUMBER,
         required = true,
         searchable = true,
         order = 4,
         formatter = CurrencyFormatter.class
     )
     @NotNull(message = "Price is required")
     @Positive(message = "Price must be positive")
     @Column(nullable = false, precision = 10, scale = 2)
     private BigDecimal price;

    @CmsField(
        displayName = "Description",
        type = CmsFieldType.TEXT,
        required = false,
        order = 5
    )
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
     * Returns the name of the entity field that corresponds to the sync key.
     *
     * @see CatalogAwareModel#getSyncKeyFieldName()
     * @return The field name.
     */
    @Override
    public String getSyncKeyFieldName() {
        return "code";
    }

    /**
     * Converts the entity to an item search result DTO.
     *
     * @see CatalogAwareModel#toItemSearchResultDTO()
     * @return The item search result DTO.
     */
    @Override
    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(getSyncKey(), getName(), getCode() + " - $" + getPrice());
    }
}
