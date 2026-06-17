package com.demo.cms.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.demo.cms.admin.dto.CreateComponentRequest;
import com.demo.cms.admin.dto.ReorderComponentRequest;
import com.demo.cms.admin.exception.BadRequestException;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.repository.ComponentRepository;
import com.demo.cms.admin.repository.SlotRepository;
import com.demo.cms.admin.service.StorefrontCacheEvictionService;
import com.demo.cms.dto.ComponentDTO;
import com.demo.cms.entity.Component;
import com.demo.cms.entity.Slot;
import com.demo.cms.entity.component.BannerComponent;
import com.demo.cms.entity.component.NavigationComponent;
import com.demo.cms.entity.component.ParagraphComponent;
import com.demo.cms.entity.component.ProductCarouselComponent;
import com.demo.cms.entity.component.QuickMenuComponent;
import com.demo.cms.entity.component.ProductDetailComponent;
import com.demo.cms.mapper.EntityMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cms/components")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class ComponentManagementController {

    private final ComponentRepository componentRepository;
    private final SlotRepository slotRepository;
    private final EntityMapper entityMapper;
    private final ObjectMapper objectMapper;
    private final StorefrontCacheEvictionService storefrontCacheEvictionService;

    @GetMapping
    public ResponseEntity<List<ComponentDTO>> getAllComponents() {
        List<Component> components = componentRepository.findAll();
        List<ComponentDTO> dtos = components.stream().map(entityMapper::toComponentDTO).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<ComponentDTO> createComponent(@Valid @RequestBody CreateComponentRequest request) {
        if (request.getSlotId() == null) {
            throw new BadRequestException("Slot ID is required to create a component");
        }
        Slot slot = slotRepository.findById(request.getSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        Component component = createComponentFromRequest(request);
        Component savedComponent = componentRepository.save(component);
        
        int index = request.getSortOrder() != null ? request.getSortOrder() : slot.getComponents().size();
        if (index > slot.getComponents().size()) index = slot.getComponents().size();
        slot.getComponents().add(index, savedComponent);
        slotRepository.save(slot);
        
        ComponentDTO dto = entityMapper.toComponentDTO(savedComponent);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/slots/{slotId}/components/{componentId}")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> linkComponent(@PathVariable Long slotId, @PathVariable Long componentId, @RequestBody Map<String, Integer> payload) {
        Slot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        Component component = componentRepository.findById(componentId)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + componentId));
            
        Integer sortOrder = payload.get("sortOrder");
        int index = sortOrder != null ? sortOrder : slot.getComponents().size();
        if (index > slot.getComponents().size()) index = slot.getComponents().size();
        
        if (!slot.getComponents().contains(component)) {
            slot.getComponents().add(index, component);
            slotRepository.save(slot);
        }
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<ComponentDTO> updateComponent(
            @PathVariable Long id,
            @Valid @RequestBody CreateComponentRequest request) {
        
        Component existing = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));

        if (!existing.getType().name().equals(request.getType())) {
            throw new BadRequestException("Cannot change component type");
        }

        updateComponentFromRequest(existing, request);
        Component updated = componentRepository.save(existing);
        
        ComponentDTO dto = entityMapper.toComponentDTO(updated);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/slots/{slotId}/components/{id}/reorder")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> reorderComponent(
            @PathVariable Long slotId,
            @PathVariable Long id,
            @Valid @RequestBody ReorderComponentRequest request) {
        
        Slot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));

        if (slot.getComponents().remove(component)) {
            int newIndex = request.getSortOrder();
            if (newIndex > slot.getComponents().size()) newIndex = slot.getComponents().size();
            slot.getComponents().add(newIndex, component);
            slotRepository.save(slot);
        }
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/slots/{slotId}/components/{id}")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> removeComponentFromSlot(@PathVariable Long slotId, @PathVariable Long id) {
        Slot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));
            
        if (slot.getComponents().remove(component)) {
            slotRepository.save(slot);
        }
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        if (!componentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Component not found with id: " + id);
        }
        componentRepository.deleteById(id);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.noContent().build();
    }

    private Component createComponentFromRequest(CreateComponentRequest request) {
        Component component;
        
        switch (request.getType()) {
            case "BANNER":
                component = createBannerComponent(request);
                break;
            case "PARAGRAPH":
                component = createParagraphComponent(request);
                break;
            case "PRODUCT_CAROUSEL":
                component = createProductCarouselComponent(request);
                break;
            case "NAVIGATION":
                component = createNavigationComponent(request);
                break;
            case "QUICK_MENU":
                component = createQuickMenuComponent(request);
                break;
            case "PRODUCT_DETAIL":
                component = createProductDetailComponent(request);
                break;
            default:
                throw new BadRequestException("Unknown component type: " + request.getType());
        }
        
        component.setUid(request.getUid());
        component.setName(request.getName());
        component.setType(request.getType());
        
        return component;
    }

    private void updateComponentFromRequest(Component existing, CreateComponentRequest request) {
        existing.setUid(request.getUid());
        existing.setName(request.getName());
        
        // Update type-specific fields
        Map<String, Object> requestMap = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        
        if (existing instanceof BannerComponent) {
            updateBannerComponent((BannerComponent) existing, requestMap);
        } else if (existing instanceof ParagraphComponent) {
            updateParagraphComponent((ParagraphComponent) existing, requestMap);
        } else if (existing instanceof ProductCarouselComponent) {
            updateProductCarouselComponent((ProductCarouselComponent) existing, requestMap);
        } else if (existing instanceof NavigationComponent) {
            updateNavigationComponent((NavigationComponent) existing, requestMap);
        } else if (existing instanceof QuickMenuComponent) {
            updateQuickMenuComponent((QuickMenuComponent) existing, requestMap);
        } else if (existing instanceof ProductDetailComponent) {
            updateProductDetailComponent((ProductDetailComponent) existing, requestMap);
        }
    }

    private BannerComponent createBannerComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        BannerComponent banner = new BannerComponent();
        banner.setImageUrl(getStringOrNull(map, "imageUrl"));
        banner.setAltText(getStringOrNull(map, "altText"));
        banner.setTitle(getStringOrNull(map, "title"));
        banner.setSubtitle(getStringOrNull(map, "subtitle"));
        banner.setCtaText(getStringOrNull(map, "ctaText"));
        banner.setCtaUrl(getStringOrNull(map, "ctaUrl"));
        return banner;
    }

    private void updateBannerComponent(BannerComponent banner, Map<String, Object> map) {
        banner.setImageUrl(getStringOrNull(map, "imageUrl"));
        banner.setAltText(getStringOrNull(map, "altText"));
        banner.setTitle(getStringOrNull(map, "title"));
        banner.setSubtitle(getStringOrNull(map, "subtitle"));
        banner.setCtaText(getStringOrNull(map, "ctaText"));
        banner.setCtaUrl(getStringOrNull(map, "ctaUrl"));
    }

    private ParagraphComponent createParagraphComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        ParagraphComponent paragraph = new ParagraphComponent();
        paragraph.setTitle(getStringOrNull(map, "title"));
        paragraph.setContent(getStringOrNull(map, "content"));
        return paragraph;
    }

    private void updateParagraphComponent(ParagraphComponent paragraph, Map<String, Object> map) {
        paragraph.setTitle(getStringOrNull(map, "title"));
        paragraph.setContent(getStringOrNull(map, "content"));
    }

    private ProductCarouselComponent createProductCarouselComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        ProductCarouselComponent carousel = new ProductCarouselComponent();
        carousel.setTitle(getStringOrNull(map, "title"));
        
        @SuppressWarnings("unchecked")
        List<String> codes = (List<String>) map.get("productCodes");
        if (codes != null) {
            carousel.setProductCodes(String.join(",", codes));
        }
        return carousel;
    }

    private void updateProductCarouselComponent(ProductCarouselComponent carousel, Map<String, Object> map) {
        carousel.setTitle(getStringOrNull(map, "title"));
        
        @SuppressWarnings("unchecked")
        List<String> codes = (List<String>) map.get("productCodes");
        if (codes != null) {
            carousel.setProductCodes(String.join(",", codes));
        }
    }

    private NavigationComponent createNavigationComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        NavigationComponent nav = new NavigationComponent();
        nav.setDisplayText(getStringOrNull(map, "displayText"));
        nav.setUrl(getStringOrNull(map, "url"));
        nav.setIcon(getStringOrNull(map, "icon"));
        return nav;
    }

    private void updateNavigationComponent(NavigationComponent nav, Map<String, Object> map) {
        nav.setDisplayText(getStringOrNull(map, "displayText"));
        nav.setUrl(getStringOrNull(map, "url"));
        nav.setIcon(getStringOrNull(map, "icon"));
    }

    private QuickMenuComponent createQuickMenuComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        QuickMenuComponent menu = new QuickMenuComponent();
        menu.setTitle(getStringOrNull(map, "title"));
        menu.setImageUrl(getStringOrNull(map, "imageUrl"));
        menu.setUrl(getStringOrNull(map, "url"));
        return menu;
    }

    private void updateQuickMenuComponent(QuickMenuComponent menu, Map<String, Object> map) {
        menu.setTitle(getStringOrNull(map, "title"));
        menu.setImageUrl(getStringOrNull(map, "imageUrl"));
        menu.setUrl(getStringOrNull(map, "url"));
    }

    private String getStringOrNull(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private ProductDetailComponent createProductDetailComponent(CreateComponentRequest request) {
        Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
        ProductDetailComponent detail = new ProductDetailComponent();
        detail.setTitle(getStringOrNull(map, "title"));
        detail.setShowPrice(getBooleanOrDefault(map, "showPrice", true));
        detail.setShowDescription(getBooleanOrDefault(map, "showDescription", true));
        return detail;
    }

    private void updateProductDetailComponent(ProductDetailComponent detail, Map<String, Object> map) {
        detail.setTitle(getStringOrNull(map, "title"));
        detail.setShowPrice(getBooleanOrDefault(map, "showPrice", true));
        detail.setShowDescription(getBooleanOrDefault(map, "showDescription", true));
    }

    private Boolean getBooleanOrDefault(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    // --- Schema Metadata Classes & Endpoints ---

    @Getter
    @AllArgsConstructor
    public static class ComponentTypeInfo {
        private String type;
        private String displayName;
        private String description;
    }

    @Getter
    @AllArgsConstructor
    public static class ComponentField {
        private String name;
        private String displayName;
        private String type; // "string", "text", "boolean", "array_string"
        private boolean required;
        private String placeholder;
    }

    @Getter
    @AllArgsConstructor
    public static class ComponentSchema {
        private String type;
        private String displayName;
        private List<ComponentField> fields;
    }

    @GetMapping("/types")
    public ResponseEntity<List<ComponentTypeInfo>> getComponentTypes() {
        return ResponseEntity.ok(List.of(
            new ComponentTypeInfo("BANNER", "Hero Banner", "Image banner with title, subtitle, and CTA button"),
            new ComponentTypeInfo("PARAGRAPH", "Paragraph Content", "Rich text content block with optional title"),
            new ComponentTypeInfo("PRODUCT_CAROUSEL", "Product Carousel", "Grid of selected products by codes"),
            new ComponentTypeInfo("NAVIGATION", "Navigation Link", "Simple link with label and URL"),
            new ComponentTypeInfo("QUICK_MENU", "Quick Menu Tile", "Visual card with title, image, and link"),
            new ComponentTypeInfo("PRODUCT_DETAIL", "Product Details", "Dynamic details layout for the current product context")
        ));
    }

    @GetMapping("/types/{type}/schema")
    public ResponseEntity<ComponentSchema> getComponentSchema(@PathVariable String type) {
        ComponentSchema schema = switch (type.toUpperCase()) {
            case "BANNER" -> new ComponentSchema("BANNER", "Hero Banner", List.of(
                new ComponentField("imageUrl", "Image URL", "string", true, "https://images.unsplash.com/..."),
                new ComponentField("altText", "Alt Text", "string", false, "Alternative text description"),
                new ComponentField("title", "Title", "string", true, "Banner Title"),
                new ComponentField("subtitle", "Subtitle", "string", false, "Banner Subtitle"),
                new ComponentField("ctaText", "CTA Text", "string", false, "Shop Now"),
                new ComponentField("ctaUrl", "CTA URL", "string", false, "/products")
            ));
            case "PARAGRAPH" -> new ComponentSchema("PARAGRAPH", "Paragraph Content", List.of(
                new ComponentField("title", "Title (Optional)", "string", false, "Section Title"),
                new ComponentField("content", "Content (HTML allowed)", "text", true, "<p>Write text here...</p>")
            ));
            case "PRODUCT_CAROUSEL" -> new ComponentSchema("PRODUCT_CAROUSEL", "Product Carousel", List.of(
                new ComponentField("title", "Carousel Title", "string", true, "Featured Products"),
                new ComponentField("productCodes", "Product Codes (comma-separated)", "array_string", true, "macbook-pro, iphone-15-pro")
            ));
            case "NAVIGATION" -> new ComponentSchema("NAVIGATION", "Navigation Link", List.of(
                new ComponentField("displayText", "Display Text", "string", true, "Link Label"),
                new ComponentField("url", "URL", "string", true, "/about-us"),
                new ComponentField("icon", "Icon Name", "string", false, "home")
            ));
            case "QUICK_MENU" -> new ComponentSchema("QUICK_MENU", "Quick Menu Tile", List.of(
                new ComponentField("title", "Tile Title", "string", true, "Promo"),
                new ComponentField("imageUrl", "Image URL", "string", true, "https://images.unsplash.com/..."),
                new ComponentField("url", "Target URL", "string", true, "/promo")
            ));
            case "PRODUCT_DETAIL" -> new ComponentSchema("PRODUCT_DETAIL", "Product Details", List.of(
                new ComponentField("title", "Title (Optional)", "string", false, "Product Details Override Title"),
                new ComponentField("showPrice", "Show Price", "boolean", false, "true"),
                new ComponentField("showDescription", "Show Description", "boolean", false, "true")
            ));
            default -> throw new BadRequestException("Unknown component type: " + type);
        };
        return ResponseEntity.ok(schema);
    }
}
