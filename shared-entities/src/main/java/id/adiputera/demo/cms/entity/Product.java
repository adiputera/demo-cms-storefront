package id.adiputera.demo.cms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import id.adiputera.demo.cms.annotation.CmsSearchable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@CmsSearchable(name = "name", displayName = "Product Name", type = "string")
@CmsSearchable(name = "code", displayName = "Product Code", type = "string")
public class Product extends CatalogAwareModel {

    @NotBlank(message = "Product code is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String code;

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Override
    public String getSyncKey() {
        return this.code;
    }
}
