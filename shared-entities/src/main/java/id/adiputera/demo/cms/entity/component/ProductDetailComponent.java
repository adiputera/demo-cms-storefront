package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsFieldType;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product Detail Component class.
 *
 * @author Yusuf F. Adiputera
 */
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
    @CmsField(displayName = "Title (Optional)", type = CmsFieldType.STRING, placeholder = "Product Details Override Title")
    private String title;

    @Column(name = "show_price")
    @CmsField(displayName = "Show Price", type = CmsFieldType.BOOLEAN, placeholder = "true")
    private Boolean showPrice;

    @Column(name = "show_description")
    @CmsField(displayName = "Show Description", type = CmsFieldType.BOOLEAN, placeholder = "true")
    private Boolean showDescription;

    @Override
    public ComponentType getType() {
        return ComponentType.PRODUCT_DETAIL;
    }
}

