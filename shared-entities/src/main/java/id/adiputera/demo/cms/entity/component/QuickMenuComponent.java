package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;
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
 * Quick Menu Component class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "quick_menu_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Quick Menu Tile", description = "Visual card with title, image, and link")
public class QuickMenuComponent extends Component {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    @CmsField(displayName = "Tile Title", type = "string", required = true, placeholder = "Promo")
    private String title;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500)
    @Column(name = "image_url", nullable = false)
    @CmsField(displayName = "Image URL", type = "image", required = true, placeholder = "https://images.unsplash.com/...")
    private String imageUrl;

    @NotBlank(message = "URL is required")
    @Size(max = 500)
    @Column(name = "url", nullable = false)
    @CmsField(displayName = "Target URL", type = "string", required = true, placeholder = "/promo")
    private String url;

    @Override
    public ComponentType getType() {
        return ComponentType.QUICK_MENU;
    }
}

