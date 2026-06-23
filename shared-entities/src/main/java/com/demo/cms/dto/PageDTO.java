package com.demo.cms.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Long id;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Breadcrumb title is required")
    private String breadcrumbTitle;

    // SEO Fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String canonicalUrl;
    private Boolean robotsIndex;
    private Boolean robotsFollow;

    // OpenGraph Fields
    private String ogTitle;
    private String ogDescription;
    private String ogImage;

    // Relationships
    private List<SlotDTO> slots;
    private List<BreadcrumbDTO> breadcrumbs;

    private String syncStatus;
}
