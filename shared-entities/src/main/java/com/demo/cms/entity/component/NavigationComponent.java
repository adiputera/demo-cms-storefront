package com.demo.cms.entity.component;

import com.demo.cms.entity.Component;
import com.demo.cms.entity.ComponentType;

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
public class NavigationComponent extends Component {

    @NotBlank(message = "Display text is required")
    @Size(max = 255)
    @Column(name = "display_text", nullable = false)
    private String displayText;

    @NotBlank(message = "URL is required")
    @Size(max = 500)
    @Column(name = "url", nullable = false)
    private String url;

    @Size(max = 100)
    @Column(name = "icon")
    private String icon;

    @Override
    public ComponentType getType() {
        return ComponentType.NAVIGATION;
    }
}
