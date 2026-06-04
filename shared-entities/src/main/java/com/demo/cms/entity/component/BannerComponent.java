package com.demo.cms.entity.component;

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
@Table(name = "banner_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerComponent extends Component {

    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "alt_text")
    private String altText;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Size(max = 500)
    @Column(name = "subtitle")
    private String subtitle;

    @Size(max = 100)
    @Column(name = "cta_text")
    private String ctaText;

    @Size(max = 500)
    @Column(name = "cta_url")
    private String ctaUrl;

    @Override
    public ComponentType getType() {
        return ComponentType.BANNER;
    }
}
