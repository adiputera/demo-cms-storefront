package com.demo.cms.entity.component;

import com.demo.cms.annotation.CmsComponent;
import com.demo.cms.annotation.CmsField;
import com.demo.cms.entity.Component;
import com.demo.cms.entity.ComponentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_carousel_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Product Carousel", description = "Grid of selected products by codes")
public class ProductCarouselComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Carousel Title", type = "string", required = true, placeholder = "Featured Products")
    private String title;

    @Column(name = "product_codes", columnDefinition = "TEXT")
    @CmsField(displayName = "Products", type = "multiple_products", required = true, placeholder = "Select products...")
    private String productCodes; // Comma-separated list of product codes

    @Override
    public ComponentType getType() {
        return ComponentType.PRODUCT_CAROUSEL;
    }
}

