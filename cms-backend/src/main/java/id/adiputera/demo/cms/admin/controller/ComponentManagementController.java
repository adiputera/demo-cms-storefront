package id.adiputera.demo.cms.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.adiputera.demo.cms.admin.dto.ComponentSchema;
import id.adiputera.demo.cms.admin.dto.ComponentTypeInfo;
import id.adiputera.demo.cms.admin.dto.CreateBannerComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateLatestArticleComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateLatestEventComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateNavigationComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateParagraphComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateProductCarouselComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateProductDetailComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateQuickMenuComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateTopEventComponentRequest;
import id.adiputera.demo.cms.admin.dto.CreateTrendingArticleComponentRequest;
import id.adiputera.demo.cms.admin.dto.ReorderComponentRequest;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.ComponentRepository;
import id.adiputera.demo.cms.admin.repository.SlotRepository;
import id.adiputera.demo.cms.dto.ComponentDTO;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.Slot;
import id.adiputera.demo.cms.entity.component.BannerComponent;
import id.adiputera.demo.cms.entity.component.LatestArticleComponent;
import id.adiputera.demo.cms.entity.component.LatestEventComponent;
import id.adiputera.demo.cms.entity.component.NavigationComponent;
import id.adiputera.demo.cms.entity.component.ParagraphComponent;
import id.adiputera.demo.cms.entity.component.ProductCarouselComponent;
import id.adiputera.demo.cms.entity.component.ProductDetailComponent;
import id.adiputera.demo.cms.entity.component.QuickMenuComponent;
import id.adiputera.demo.cms.entity.component.TopEventComponent;
import id.adiputera.demo.cms.entity.component.TrendingArticleComponent;
import id.adiputera.demo.cms.mapper.EntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Component Management Controller class.
 *
 * @author Yusuf F. Adiputera
 */
@RestController
@RequestMapping("/api/cms/components")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class ComponentManagementController {

    private final ComponentRepository componentRepository;
    private final SlotRepository slotRepository;
    private final id.adiputera.demo.cms.admin.repository.CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final ObjectMapper objectMapper;

    private final id.adiputera.demo.cms.admin.service.ComponentSchemaService componentSchemaService;
    private final id.adiputera.demo.cms.admin.service.CatalogSyncService catalogSyncService;

    private id.adiputera.demo.cms.entity.Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
            .orElseGet(() -> {
                id.adiputera.demo.cms.entity.Catalog cat = new id.adiputera.demo.cms.entity.Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(id.adiputera.demo.cms.entity.CatalogVersion.STAGED);
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
            case "TRENDING_ARTICLE":
                component = createTrendingArticleComponent(request);
                break;
            case "LATEST_EVENT":
                component = createLatestEventComponent(request);
                break;
            case "TOP_EVENT":
                component = createTopEventComponent(request);
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
        } else if (existing instanceof LatestEventComponent latestEvent) {
            updateLatestEventComponent(latestEvent, request);
        } else if (existing instanceof TrendingArticleComponent trendingArticle && request instanceof CreateTrendingArticleComponentRequest req) {
            updateTrendingArticleComponent(trendingArticle, req);
        } else if (existing instanceof TopEventComponent topEvent && request instanceof CreateTopEventComponentRequest req) {
            updateTopEventComponent(topEvent, req);
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

    /**
     * Creates a new ProductCarouselComponent from the request.
     *
     * @param request The creation request.
     * @return The created component.
     */
    private ProductCarouselComponent createProductCarouselComponent(CreateComponentRequest request) {
        CreateProductCarouselComponentRequest req = (CreateProductCarouselComponentRequest) request;
        ProductCarouselComponent carousel = new ProductCarouselComponent();
        carousel.setTitle(req.getTitle());
        if (req.getProductCodes() != null) {
            carousel.setProductCodes(String.join(",", req.getProductCodes()));
        }
        return carousel;
    }

    /**
     * Updates an existing ProductCarouselComponent.
     *
     * @param carousel The component to update.
     * @param req The update request.
     */
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

    private LatestEventComponent createLatestEventComponent(CreateComponentRequest request) {
        CreateLatestEventComponentRequest req = objectMapper.convertValue(request, CreateLatestEventComponentRequest.class);
        LatestEventComponent component = new LatestEventComponent();
        component.setTitle(req.getTitle());
        if (req.getEventSlugs() != null) {
            component.setEventSlugs(String.join(",", req.getEventSlugs()));
        }
        return component;
    }

    private void updateLatestEventComponent(LatestEventComponent component, CreateComponentRequest request) {
        CreateLatestEventComponentRequest req = objectMapper.convertValue(request, CreateLatestEventComponentRequest.class);
        if (req.getTitle() != null) component.setTitle(req.getTitle());
        if (req.getEventSlugs() != null) {
            component.setEventSlugs(String.join(",", req.getEventSlugs()));
        } else {
            component.setEventSlugs(null);
        }
    }

    private TrendingArticleComponent createTrendingArticleComponent(CreateComponentRequest request) {
        CreateTrendingArticleComponentRequest req = (CreateTrendingArticleComponentRequest) request;
        TrendingArticleComponent trending = new TrendingArticleComponent();
        trending.setTitle(req.getTitle());
        if (req.getArticleSlugs() != null) {
            trending.setArticleSlugs(String.join(",", req.getArticleSlugs()));
        }
        return trending;
    }

    private void updateTrendingArticleComponent(TrendingArticleComponent trending, CreateTrendingArticleComponentRequest req) {
        trending.setTitle(req.getTitle());
        if (req.getArticleSlugs() != null) {
            trending.setArticleSlugs(String.join(",", req.getArticleSlugs()));
        } else {
            trending.setArticleSlugs(null);
        }
    }

    private TopEventComponent createTopEventComponent(CreateComponentRequest request) {
        CreateTopEventComponentRequest req = (CreateTopEventComponentRequest) request;
        TopEventComponent topEvent = new TopEventComponent();
        topEvent.setTitle(req.getTitle());
        topEvent.setEventSlug(req.getEventSlug());
        return topEvent;
    }

    private void updateTopEventComponent(TopEventComponent topEvent, CreateTopEventComponentRequest req) {
        topEvent.setTitle(req.getTitle());
        topEvent.setEventSlug(req.getEventSlug());
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
