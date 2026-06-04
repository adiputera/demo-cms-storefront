package com.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BannerComponentDTO extends ComponentDTO {
    private String imageUrl;
    private String altText;
    private String title;
    private String subtitle;
    private String ctaText;
    private String ctaUrl;
}
