package com.demo.cms.admin.controller;

import java.util.List;
import java.util.Map;


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

import com.demo.cms.admin.dto.ComponentField;
import com.demo.cms.admin.dto.ComponentSchema;
import com.demo.cms.admin.dto.ComponentTypeInfo;
import com.demo.cms.admin.dto.CreateComponentRequest;
import com.demo.cms.admin.dto.CreateLatestArticleComponentRequest;
import com.demo.cms.admin.dto.CreateBannerComponentRequest;
import com.demo.cms.admin.dto.CreateParagraphComponentRequest;
import com.demo.cms.admin.dto.CreateProductCarouselComponentRequest;
import com.demo.cms.admin.dto.CreateNavigationComponentRequest;
import com.demo.cms.admin.dto.CreateQuickMenuComponentRequest;
import com.demo.cms.admin.dto.CreateProductDetailComponentRequest;
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
import com.demo.cms.entity.component.LatestArticleComponent;
import com.demo.cms.entity.component.ProductDetailComponent;
import com.demo.cms.mapper.EntityMapper;
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
    private final com.demo.cms.admin.repository.CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final ObjectMapper objectMapper;

    private final com.demo.cms.admin.service.ComponentSchemaService componentSchemaService;
    private final com.demo.cms.admin.service.CatalogSyncService catalogSyncService;

    private com.demo.cms.entity.Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", com.demo.cms.entity.CatalogVersion.STAGED)
            .orElseGet(() -> {
                com.demo.cms.entity.Catalog cat = new com.demo.cms.entity.Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(com.demo.cms.entity.CatalogVersion.STAGED);
                return catalogRepository.save(cat);
            });
    }

    @GetMapping
    public ResponseEntity<List<ComponentDTO>> getAllComponents() {
        List<Component> components = componentRepository.findAll();
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(components, Component.class);
        
        List<ComponentDTO> dtos = components.stream().map(c -> {
            ComponentDTO dto = entityMapper.toComponentDTO(c);
            dto.setSyncStatus(syncStatusMap.getOrDefault(c.getSyncKey(), "UNKNOWN"));
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
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
        
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(List.of(savedComponent), Component.class);
        ComponentDTO dto = entityMapper.toComponentDTO(savedComponent);
        dto.setSyncStatus(syncStatusMap.getOrDefault(savedComponent.getSyncKey(), "UNKNOWN"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/slots/{slotId}/components/{componentId}")
    @Transactional
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
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
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
        
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(List.of(updated), Component.class);
        ComponentDTO dto = entityMapper.toComponentDTO(updated);
        dto.setSyncStatus(syncStatusMap.getOrDefault(updated.getSyncKey(), "UNKNOWN"));
        
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/slots/{slotId}/components/{id}/reorder")
    @Transactional
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
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/slots/{slotId}/components/{id}")
    @Transactional
    public ResponseEntity<Void> removeComponentFromSlot(@PathVariable Long slotId, @PathVariable Long id) {
        Slot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + id));
            
        if (slot.getComponents().remove(component)) {
            slotRepository.save(slot);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        if (!componentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Component not found with id: " + id);
        }
        componentRepository.deleteById(id);
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
            case "LATEST_ARTICLE":
                component = createLatestArticleComponent(request);
                break;
            default:
                throw new BadRequestException("Unknown component type: " + request.getType());
        }
        
        component.setUid(request.getUid());
        component.setName(request.getName());
        component.setType(request.getType());
        component.setCatalog(getStagedCatalog());
        
        return component;
    }

    private void updateComponentFromRequest(Component existing, CreateComponentRequest request) {
        existing.setUid(request.getUid());
        existing.setName(request.getName());
        
        if (existing instanceof BannerComponent banner && request instanceof CreateBannerComponentRequest req) {
            updateBannerComponent(banner, req);
        } else if (existing instanceof ParagraphComponent paragraph && request instanceof CreateParagraphComponentRequest req) {
            updateParagraphComponent(paragraph, req);
        } else if (existing instanceof ProductCarouselComponent carousel && request instanceof CreateProductCarouselComponentRequest req) {
            updateProductCarouselComponent(carousel, req);
        } else if (existing instanceof NavigationComponent nav && request instanceof CreateNavigationComponentRequest req) {
            updateNavigationComponent(nav, req);
        } else if (existing instanceof QuickMenuComponent menu && request instanceof CreateQuickMenuComponentRequest req) {
            updateQuickMenuComponent(menu, req);
        } else if (existing instanceof ProductDetailComponent detail && request instanceof CreateProductDetailComponentRequest req) {
            updateProductDetailComponent(detail, req);
        } else if (existing instanceof LatestArticleComponent latestArticle) {
            updateLatestArticleComponent(latestArticle, request);
        }
    }

    private BannerComponent createBannerComponent(CreateComponentRequest request) {
        CreateBannerComponentRequest req = (CreateBannerComponentRequest) request;
        BannerComponent banner = new BannerComponent();
        banner.setImageUrl(req.getImageUrl());
        banner.setAltText(req.getAltText());
        banner.setTitle(req.getTitle());
        banner.setSubtitle(req.getSubtitle());
        banner.setCtaText(req.getCtaText());
        banner.setCtaUrl(req.getCtaUrl());
        return banner;
    }

    private void updateBannerComponent(BannerComponent banner, CreateBannerComponentRequest req) {
        banner.setImageUrl(req.getImageUrl());
        banner.setAltText(req.getAltText());
        banner.setTitle(req.getTitle());
        banner.setSubtitle(req.getSubtitle());
        banner.setCtaText(req.getCtaText());
        banner.setCtaUrl(req.getCtaUrl());
    }

    private ParagraphComponent createParagraphComponent(CreateComponentRequest request) {
        CreateParagraphComponentRequest req = (CreateParagraphComponentRequest) request;
        ParagraphComponent paragraph = new ParagraphComponent();
        paragraph.setTitle(req.getTitle());
        paragraph.setContent(req.getContent());
        return paragraph;
    }

    private void updateParagraphComponent(ParagraphComponent paragraph, CreateParagraphComponentRequest req) {
        paragraph.setTitle(req.getTitle());
        paragraph.setContent(req.getContent());
    }

    private ProductCarouselComponent createProductCarouselComponent(CreateComponentRequest request) {
        CreateProductCarouselComponentRequest req = (CreateProductCarouselComponentRequest) request;
        ProductCarouselComponent carousel = new ProductCarouselComponent();
        carousel.setTitle(req.getTitle());
        if (req.getProductCodes() != null) {
            carousel.setProductCodes(String.join(",", req.getProductCodes()));
        }
        return carousel;
    }

    private void updateProductCarouselComponent(ProductCarouselComponent carousel, CreateProductCarouselComponentRequest req) {
        carousel.setTitle(req.getTitle());
        if (req.getProductCodes() != null) {
            carousel.setProductCodes(String.join(",", req.getProductCodes()));
        }
    }

    private NavigationComponent createNavigationComponent(CreateComponentRequest request) {
        CreateNavigationComponentRequest req = (CreateNavigationComponentRequest) request;
        NavigationComponent nav = new NavigationComponent();
        nav.setDisplayText(req.getDisplayText());
        nav.setUrl(req.getUrl());
        nav.setIcon(req.getIcon());
        return nav;
    }

    private void updateNavigationComponent(NavigationComponent nav, CreateNavigationComponentRequest req) {
        nav.setDisplayText(req.getDisplayText());
        nav.setUrl(req.getUrl());
        nav.setIcon(req.getIcon());
    }

    private QuickMenuComponent createQuickMenuComponent(CreateComponentRequest request) {
        CreateQuickMenuComponentRequest req = (CreateQuickMenuComponentRequest) request;
        QuickMenuComponent menu = new QuickMenuComponent();
        menu.setTitle(req.getTitle());
        menu.setImageUrl(req.getImageUrl());
        menu.setUrl(req.getUrl());
        return menu;
    }

    private void updateQuickMenuComponent(QuickMenuComponent menu, CreateQuickMenuComponentRequest req) {
        menu.setTitle(req.getTitle());
        menu.setImageUrl(req.getImageUrl());
        menu.setUrl(req.getUrl());
    }

    private ProductDetailComponent createProductDetailComponent(CreateComponentRequest request) {
        CreateProductDetailComponentRequest req = (CreateProductDetailComponentRequest) request;
        ProductDetailComponent detail = new ProductDetailComponent();
        detail.setTitle(req.getTitle());
        detail.setShowPrice(req.getShowPrice() != null ? req.getShowPrice() : true);
        detail.setShowDescription(req.getShowDescription() != null ? req.getShowDescription() : true);
        return detail;
    }

    private void updateProductDetailComponent(ProductDetailComponent detail, CreateProductDetailComponentRequest req) {
        detail.setTitle(req.getTitle());
        detail.setShowPrice(req.getShowPrice() != null ? req.getShowPrice() : true);
        detail.setShowDescription(req.getShowDescription() != null ? req.getShowDescription() : true);
    }

    private LatestArticleComponent createLatestArticleComponent(CreateComponentRequest request) {
        CreateLatestArticleComponentRequest req = objectMapper.convertValue(request, CreateLatestArticleComponentRequest.class);
        return LatestArticleComponent.builder()
                .title(req.getTitle())
                .articleCount(req.getArticleCount())
                .build();
    }

    private void updateLatestArticleComponent(LatestArticleComponent component, CreateComponentRequest request) {
        CreateLatestArticleComponentRequest req = objectMapper.convertValue(request, CreateLatestArticleComponentRequest.class);
        if (req.getTitle() != null) component.setTitle(req.getTitle());
        if (req.getArticleCount() != null) component.setArticleCount(req.getArticleCount());
    }

    @GetMapping("/types")
    public ResponseEntity<List<ComponentTypeInfo>> getComponentTypes() {
        return ResponseEntity.ok(componentSchemaService.getComponentTypes());
    }

    @GetMapping("/types/{type}/schema")
    public ResponseEntity<ComponentSchema> getComponentSchema(@PathVariable String type) {
        return ResponseEntity.ok(componentSchemaService.getComponentSchema(type));
    }
}
