package com.demo.cms.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.cms.admin.dto.CreateComponentRequest;
import com.demo.cms.admin.dto.ReorderComponentRequest;
import com.demo.cms.admin.exception.BadRequestException;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.repository.ComponentRepository;
import com.demo.cms.admin.repository.SlotRepository;
import com.demo.cms.dto.ComponentDTO;
import com.demo.cms.entity.Component;
import com.demo.cms.entity.Slot;
import com.demo.cms.entity.component.BannerComponent;
import com.demo.cms.entity.component.NavigationComponent;
import com.demo.cms.entity.component.ParagraphComponent;
import com.demo.cms.entity.component.ProductCarouselComponent;
import com.demo.cms.entity.component.QuickMenuComponent;
import com.demo.cms.mapper.EntityMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cms/components")
@RequiredArgsConstructor
@Validated
public class ComponentManagementController {

    private final ComponentRepository componentRepository;
    private final SlotRepository slotRepository;
    private final EntityMapper entityMapper;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<ComponentDTO> createComponent(@Valid @RequestBody CreateComponentRequest request) {
        Slot slot = slotRepository.findById(request.getSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        Component component = createComponentFromRequest(request, slot);
        Component savedComponent = componentRepository.save(component);
        
        ComponentDTO dto = entityMapper.toComponentDTO(savedComponent);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<ComponentDTO> updateComponent(
            @PathVariable Long id,
            @Valid @RequestBody CreateComponentRequest request) {
        
        Component existing = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));

        // Verify type hasn't changed
        if (!existing.getType().name().equals(request.getType())) {
            throw new BadRequestException("Cannot change component type");
        }

        updateComponentFromRequest(existing, request);
        Component updated = componentRepository.save(existing);
        
        ComponentDTO dto = entityMapper.toComponentDTO(updated);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/reorder")
    @Transactional
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> reorderComponent(
            @PathVariable Long id,
            @Valid @RequestBody ReorderComponentRequest request) {
        
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));

        component.setSortOrder(request.getSortOrder());
        componentRepository.save(component);
        
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        if (!componentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Component not found with id: " + id);
        }
        componentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Component createComponentFromRequest(CreateComponentRequest request, Slot slot) {
        Component component;
        
        switch (request.getType()) {
            case "BANNER":
                component = createBannerComponent(request, slot);
                break;
            case "PARAGRAPH":
                component = createParagraphComponent(request, slot);
                break;
            case "PRODUCT_CAROUSEL":
                component = createProductCarouselComponent(request, slot);
                break;
            case "NAVIGATION":
                component = createNavigationComponent(request, slot);
                break;
            case "QUICK_MENU":
                component = createQuickMenuComponent(request, slot);
                break;
            default:
                throw new BadRequestException("Unknown component type: " + request.getType());
        }
        
        component.setUid(request.getUid());
        component.setName(request.getName());
        // Set type explicitly from request
        component.setType(request.getType());
        component.setSortOrder(request.getSortOrder());
        component.setSlot(slot);
        
        return component;
    }

    private void updateComponentFromRequest(Component existing, CreateComponentRequest request) {
        existing.setUid(request.getUid());
        existing.setName(request.getName());
        existing.setSortOrder(request.getSortOrder());
        
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
        }
    }

    private BannerComponent createBannerComponent(CreateComponentRequest request, Slot slot) {
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

    private ParagraphComponent createParagraphComponent(CreateComponentRequest request, Slot slot) {
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

    private ProductCarouselComponent createProductCarouselComponent(CreateComponentRequest request, Slot slot) {
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

    private NavigationComponent createNavigationComponent(CreateComponentRequest request, Slot slot) {
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

    private QuickMenuComponent createQuickMenuComponent(CreateComponentRequest request, Slot slot) {
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
}
