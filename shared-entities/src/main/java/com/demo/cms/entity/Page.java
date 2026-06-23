package com.demo.cms.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pages", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pages_slug_catalog", columnNames = {"slug", "catalog_id"})
}, indexes = {
    @Index(name = "idx_pages_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page extends CatalogAwareModel {

    @Override
    public String getSyncKey() {
        return getSlug();
    }

    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String slug;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Breadcrumb title is required")
    @Size(max = 255)
    @Column(name = "breadcrumb_title", nullable = false)
    private String breadcrumbTitle;

    // SEO Fields
    @Size(max = 255)
    @Column(name = "meta_title")
    private String metaTitle;

    @Size(max = 500)
    @Column(name = "meta_description")
    private String metaDescription;

    @Size(max = 500)
    @Column(name = "meta_keywords")
    private String metaKeywords;

    @Size(max = 500)
    @Column(name = "canonical_url")
    private String canonicalUrl;

    @Column(name = "robots_index", nullable = false)
    @Builder.Default
    private Boolean robotsIndex = true;

    @Column(name = "robots_follow", nullable = false)
    @Builder.Default
    private Boolean robotsFollow = true;

    // OpenGraph Fields
    @Size(max = 255)
    @Column(name = "og_title")
    private String ogTitle;

    @Size(max = 500)
    @Column(name = "og_description")
    private String ogDescription;

    @Size(max = 500)
    @Column(name = "og_image")
    private String ogImage;

    // Relationships
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Slot> slots = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "page_breadcrumbs",
        joinColumns = @JoinColumn(name = "page_id"),
        inverseJoinColumns = @JoinColumn(name = "breadcrumb_page_id")
    )
    @OrderColumn(name = "breadcrumb_order")
    @Builder.Default
    private List<Page> breadcrumbs = new ArrayList<>();

}
