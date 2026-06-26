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

@Entity
@Table(name = "navigation_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Navigation Link", description = "Simple link with label and URL")
public class NavigationComponent extends Component {

    @NotBlank(message = "Display text is required")
    @Size(max = 255)
    @Column(name = "display_text", nullable = false)
    @CmsField(displayName = "Display Text", type = "string", required = true, placeholder = "Link Label")
    private String displayText;

    @NotBlank(message = "URL is required")
    @Size(max = 500)
    @Column(name = "url", nullable = false)
    @CmsField(displayName = "URL", type = "string", required = true, placeholder = "/about-us")
    private String url;

    @Size(max = 100)
    @Column(name = "icon")
    @CmsField(displayName = "Icon Name", type = "string", required = false, placeholder = "home")
    private String icon;

    @Override
    public ComponentType getType() {
        return ComponentType.NAVIGATION;
    }
}

