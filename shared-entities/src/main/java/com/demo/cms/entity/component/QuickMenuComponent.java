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
@Table(name = "quick_menu_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickMenuComponent extends Component {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500)
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @NotBlank(message = "URL is required")
    @Size(max = 500)
    @Column(name = "url", nullable = false)
    private String url;

    @Override
    public ComponentType getType() {
        return ComponentType.QUICK_MENU;
    }
}
