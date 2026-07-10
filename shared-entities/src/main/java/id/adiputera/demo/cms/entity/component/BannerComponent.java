package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
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
 * Banner Component class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "banner_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Hero Banner", description = "Image banner with title, subtitle, and CTA button")
public class BannerComponent extends Component {

    @Size(max = 500)
    @Column(name = "image_url")
    @CmsField(
        displayName = "Image URL",
        type = CmsFieldType.IMAGE,
        required = true,
        placeholder = "https://images.unsplash.com/..."
    )
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "alt_text")
    @CmsField(
        displayName = "Alt Text",
        type = CmsFieldType.STRING,
        placeholder = "Alternative text description"
    )
    private String altText;

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(
        displayName = "Title",
        type = CmsFieldType.STRING,
        required = true,
        placeholder = "Banner Title"
    )
    private String title;

    @Size(max = 500)
    @Column(name = "subtitle")
    @CmsField(
        displayName = "Subtitle",
        type = CmsFieldType.STRING,
        placeholder = "Banner Subtitle"
    )
    private String subtitle;

    @Size(max = 100)
    @Column(name = "cta_text")
    @CmsField(
        displayName = "CTA Text",
        type = CmsFieldType.STRING,
        placeholder = "Shop Now"
    )
    private String ctaText;

    @Size(max = 500)
    @Column(name = "cta_url")
    @CmsField(
        displayName = "CTA URL",
        type = CmsFieldType.STRING,
        placeholder = "/products"
    )
    private String ctaUrl;

    @Override
    public ComponentType getType() {
        return ComponentType.BANNER;
    }
}

