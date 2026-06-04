package com.demo.cms.mapper;

import com.demo.cms.dto.*;
import com.demo.cms.entity.*;
import com.demo.cms.entity.component.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // Page Mapping
    public PageDTO toPageDTO(Page page, boolean includeSlots) {
        if (page == null) {
            return null;
        }

        PageDTO.PageDTOBuilder builder = PageDTO.builder()
                .id(page.getId())
                .slug(page.getSlug())
                .title(page.getTitle())
                .breadcrumbTitle(page.getBreadcrumbTitle())
                .metaTitle(page.getMetaTitle())
                .metaDescription(page.getMetaDescription())
                .metaKeywords(page.getMetaKeywords())
                .canonicalUrl(page.getCanonicalUrl())
                .robotsIndex(page.getRobotsIndex())
                .robotsFollow(page.getRobotsFollow())
                .ogTitle(page.getOgTitle())
                .ogDescription(page.getOgDescription())
                .ogImage(page.getOgImage());

        // Map breadcrumbs
        if (page.getBreadcrumbs() != null) {
            List<BreadcrumbDTO> breadcrumbs = page.getBreadcrumbs().stream()
                    .map(this::toBreadcrumbDTO)
                    .collect(Collectors.toList());
            builder.breadcrumbs(breadcrumbs);
        }

        // Map slots (metadata only, no components)
        if (includeSlots && page.getSlots() != null) {
            List<SlotDTO> slots = page.getSlots().stream()
                    .map(slot -> SlotDTO.builder()
                            .id(slot.getId())
                            .code(slot.getCode())
                            .name(slot.getName())
                            .build())
                    .collect(Collectors.toList());
            builder.slots(slots);
        }

        return builder.build();
    }

    // Breadcrumb Mapping
    public BreadcrumbDTO toBreadcrumbDTO(Page page) {
        if (page == null) {
            return null;
        }

        return BreadcrumbDTO.builder()
                .slug(page.getSlug())
                .breadcrumbTitle(page.getBreadcrumbTitle())
                .build();
    }

    // Slot Mapping with Components
    public SlotDTO toSlotDTOWithComponents(Slot slot) {
        if (slot == null) {
            return null;
        }

        List<ComponentDTO> components = slot.getComponents() != null
                ? slot.getComponents().stream()
                        .map(this::toComponentDTO)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return SlotDTO.builder()
                .id(slot.getId())
                .code(slot.getCode())
                .name(slot.getName())
                .components(components)
                .build();
    }

    // Component Mapping (Polymorphic)
    public ComponentDTO toComponentDTO(com.demo.cms.entity.Component component) {
        if (component == null) {
            return null;
        }

        ComponentType type = component.getType();

        return switch (type) {
            case PARAGRAPH -> toParagraphComponentDTO((ParagraphComponent) component);
            case BANNER -> toBannerComponentDTO((BannerComponent) component);
            case PRODUCT_CAROUSEL -> toProductCarouselComponentDTO((ProductCarouselComponent) component);
            case NAVIGATION -> toNavigationComponentDTO((NavigationComponent) component);
            case QUICK_MENU -> toQuickMenuComponentDTO((QuickMenuComponent) component);
        };
    }

    private ParagraphComponentDTO toParagraphComponentDTO(ParagraphComponent component) {
        return ParagraphComponentDTO.builder()
                .id(component.getId())
                .uid(component.getUid())
                .name(component.getName())
                .type(component.getType().name())
                .sortOrder(component.getSortOrder())
                .title(component.getTitle())
                .content(component.getContent())
                .build();
    }

    private BannerComponentDTO toBannerComponentDTO(BannerComponent component) {
        return BannerComponentDTO.builder()
                .id(component.getId())
                .uid(component.getUid())
                .name(component.getName())
                .type(component.getType().name())
                .sortOrder(component.getSortOrder())
                .imageUrl(component.getImageUrl())
                .altText(component.getAltText())
                .title(component.getTitle())
                .subtitle(component.getSubtitle())
                .ctaText(component.getCtaText())
                .ctaUrl(component.getCtaUrl())
                .build();
    }

    private ProductCarouselComponentDTO toProductCarouselComponentDTO(ProductCarouselComponent component) {
        List<String> productCodes = component.getProductCodes() != null
                ? Arrays.asList(component.getProductCodes().split(","))
                : Collections.emptyList();

        return ProductCarouselComponentDTO.builder()
                .id(component.getId())
                .uid(component.getUid())
                .name(component.getName())
                .type(component.getType().name())
                .sortOrder(component.getSortOrder())
                .title(component.getTitle())
                .productCodes(productCodes)
                .build();
    }

    private NavigationComponentDTO toNavigationComponentDTO(NavigationComponent component) {
        return NavigationComponentDTO.builder()
                .id(component.getId())
                .uid(component.getUid())
                .name(component.getName())
                .type(component.getType().name())
                .sortOrder(component.getSortOrder())
                .displayText(component.getDisplayText())
                .url(component.getUrl())
                .icon(component.getIcon())
                .build();
    }

    private QuickMenuComponentDTO toQuickMenuComponentDTO(QuickMenuComponent component) {
        return QuickMenuComponentDTO.builder()
                .id(component.getId())
                .uid(component.getUid())
                .name(component.getName())
                .type(component.getType().name())
                .sortOrder(component.getSortOrder())
                .title(component.getTitle())
                .imageUrl(component.getImageUrl())
                .url(component.getUrl())
                .build();
    }

    // Product Mapping
    public ProductDTO toProductDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }

    public List<ProductDTO> toProductDTOList(List<Product> products) {
        if (products == null) {
            return Collections.emptyList();
        }

        return products.stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }
}
