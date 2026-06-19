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
@Table(name = "product_detail_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Product Details", description = "Dynamic details layout for the current product context")
public class ProductDetailComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Title (Optional)", type = "string", required = false, placeholder = "Product Details Override Title")
    private String title;

    @Column(name = "show_price")
    @CmsField(displayName = "Show Price", type = "boolean", required = false, placeholder = "true")
    private Boolean showPrice;

    @Column(name = "show_description")
    @CmsField(displayName = "Show Description", type = "boolean", required = false, placeholder = "true")
    private Boolean showDescription;

    @Override
    public ComponentType getType() {
        return ComponentType.PRODUCT_DETAIL;
    }
}

