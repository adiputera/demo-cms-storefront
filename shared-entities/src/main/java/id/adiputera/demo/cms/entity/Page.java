package id.adiputera.demo.cms.entity;

import java.util.ArrayList;
import java.util.List;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Page class.
 *
 * @author Yusuf F. Adiputera
 */
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
        return slug;
    }

    @Override
    public String getSyncKeyFieldName() {
        return "slug";
    }

    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false)
    @CmsField(
        displayName = "Page Slug",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 1
    )
    private String slug;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    @CmsField(
        displayName = "Page Title",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 2
    )
    private String title;

    @NotBlank(message = "Breadcrumb title is required")
    @Size(max = 255)
    @Column(name = "breadcrumb_title", nullable = false)
    @CmsField(
        displayName = "Breadcrumb Title",
        type = CmsFieldType.STRING,
        required = true,
        showAsColumn = false,
        order = 3
    )
    private String breadcrumbTitle;

    // SEO Fields
    @Size(max = 255)
    @Column(name = "meta_title")
    @CmsField(
        displayName = "Meta Title",
        type = CmsFieldType.STRING,
        showAsColumn = false,
        order = 4
    )
    private String metaTitle;

    @Size(max = 500)
    @Column(name = "meta_description")
    @CmsField(
        displayName = "Meta Description",
        type = CmsFieldType.TEXT,
        showAsColumn = false,
        order = 5
    )
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
