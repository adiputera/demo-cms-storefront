package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.CreateSlotRequest;
import id.adiputera.demo.cms.admin.dto.ReorderSlotRequest;
import id.adiputera.demo.cms.admin.dto.SlotResponse;
import id.adiputera.demo.cms.admin.dto.UpdateSlotRequest;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.PageRepository;
import id.adiputera.demo.cms.admin.repository.SlotRepository;
import id.adiputera.demo.cms.dto.ComponentDTO;
import id.adiputera.demo.cms.entity.Page;
import id.adiputera.demo.cms.entity.Slot;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Slot Management Controller class.
 *
 * @author Yusuf F. Adiputera
 */
@RestController
@RequestMapping("/api/cms/slots")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class SlotManagementController {

    private final SlotRepository slotRepository;
    private final PageRepository pageRepository;
    private final id.adiputera.demo.cms.admin.repository.CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final id.adiputera.demo.cms.admin.service.CatalogSyncService catalogSyncService;

    /**
     * Retrieves or creates the STAGED catalog.
     *
     * @return The STAGED catalog entity.
     */
    private id.adiputera.demo.cms.entity.Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
            .orElseGet(() -> {
                id.adiputera.demo.cms.entity.Catalog cat = new id.adiputera.demo.cms.entity.Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(id.adiputera.demo.cms.entity.CatalogVersion.STAGED);
                return catalogRepository.save(cat);
            });
    }

    /**
     * Retrieves all slots across all pages.
     *
     * @return A list of all slot responses.
     */
    @GetMapping
    public ResponseEntity<List<SlotResponse>> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();

        java.util.Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(slots, Slot.class);

        List<id.adiputera.demo.cms.entity.Component> allComponents = slots.stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(s -> s.getComponents() != null ? s.getComponents().stream().filter(java.util.Objects::nonNull) : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
        java.util.Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(allComponents, id.adiputera.demo.cms.entity.Component.class);

        List<SlotResponse> responses = slots.stream()
                .filter(java.util.Objects::nonNull)
                .map(slot -> mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all slots belonging to a specific page.
     *
     * @param pageId The ID of the page.
     * @return A list of slot responses for the page.
     */
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<SlotResponse>> getSlotsByPage(@PathVariable Long pageId) {
        List<Slot> slots = slotRepository.findByPageId(pageId);
        
        java.util.Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(slots, Slot.class);
        
        // Also calculate for components
        List<id.adiputera.demo.cms.entity.Component> allComponents = slots.stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(s -> s.getComponents() != null ? s.getComponents().stream().filter(java.util.Objects::nonNull) : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
        java.util.Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(allComponents, id.adiputera.demo.cms.entity.Component.class);

        List<SlotResponse> responses = slots.stream()
            .filter(java.util.Objects::nonNull)
            .map(slot -> mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a slot by its ID.
     *
     * @param id The slot ID.
     * @return The slot response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlot(@PathVariable Long id) {
        Slot slot = slotRepository.findByIdWithComponents(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
            
        java.util.Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(List.of(slot), Slot.class);
        java.util.Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(
                new java.util.ArrayList<>(slot.getComponents()), id.adiputera.demo.cms.entity.Component.class);
                
        return ResponseEntity.ok(mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus));
    }

    /**
     * Creates a new slot.
     *
     * @param request The create slot request containing code, name, and page ID.
     * @return The created slot response.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        Page page = pageRepository.findById(request.getPageId())
            .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + request.getPageId()));

        Slot slot = Slot.builder()
            .code(request.getCode())
            .name(request.getName())
            .page(page)
            .build();
        slot.setCatalog(getStagedCatalog());

        Slot savedSlot = slotRepository.save(slot);
        
        // Add to page slots at specific position if sortOrder provided
        int index = request.getSortOrder() != null ? request.getSortOrder() : page.getSlots().size();
        if (index > page.getSlots().size()) index = page.getSlots().size();
        page.getSlots().add(index, savedSlot);
        pageRepository.save(page);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSlotResponse(savedSlot, new java.util.HashMap<>(), new java.util.HashMap<>()));
    }

    /**
     * Updates an existing slot.
     *
     * @param id The ID of the slot to update.
     * @param request The update slot request containing the new code and name.
     * @return The updated slot response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SlotResponse> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSlotRequest request) {
        
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        slot.setCode(request.getCode());
        slot.setName(request.getName());

        Slot updatedSlot = slotRepository.save(slot);
        return ResponseEntity.ok(mapToSlotResponse(updatedSlot, new java.util.HashMap<>(), new java.util.HashMap<>()));
    }

    /**
     * Reorders a slot within its assigned page.
     *
     * @param id The ID of the slot to reorder.
     * @param request The request specifying the new sort order index.
     * @return A response entity with no content.
     */
    @PutMapping("/{id}/reorder")
    @Transactional
    public ResponseEntity<Void> reorderSlot(
            @PathVariable Long id,
            @Valid @RequestBody ReorderSlotRequest request) {
        
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
        Page page = slot.getPage();
        
        // Get current position and new position
        List<Slot> slots = page.getSlots();
        int currentIndex = slots.indexOf(slot);
        int newIndex = request.getSortOrder();
        
        if (currentIndex == -1 || newIndex < 0 || newIndex >= slots.size()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (currentIndex != newIndex) {
            // Remove from current position and insert at new position
            slots.remove(currentIndex);
            slots.add(newIndex, slot);
            
            // Save will trigger @OrderColumn update
            pageRepository.save(page);
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a slot by its ID.
     *
     * @param id The ID of the slot to delete.
     * @return A response entity with no content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        if (!slotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Slot not found with id: " + id);
        }
        slotRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps a Slot entity to a SlotResponse DTO using the provided sync status maps.
     *
     * @param slot The slot entity to map.
     * @param slotSyncStatus A map of slot sync keys to their synchronization status.
     * @param componentSyncStatus A map of component sync keys to their synchronization status.
     * @return The mapped SlotResponse.
     */
    private SlotResponse mapToSlotResponse(Slot slot, java.util.Map<String, String> slotSyncStatus, java.util.Map<String, String> componentSyncStatus) {
        if (slot == null) return null;
        java.util.List<ComponentDTO> componentDTOs = new java.util.ArrayList<>();
        if (slot.getComponents() != null) {
            int sortOrder = 0;
            for (id.adiputera.demo.cms.entity.Component component : slot.getComponents()) {
                if (component == null) continue;
                ComponentDTO dto = entityMapper.toComponentDTO(component);
                if (dto != null) {
                    dto.setSortOrder(sortOrder++);
                    dto.setSyncStatus(componentSyncStatus.getOrDefault(component.getSyncKey(), "UNKNOWN"));
                    componentDTOs.add(dto);
                }
            }
        }

        return SlotResponse.builder()
            .id(slot.getId())
            .code(slot.getCode())
            .name(slot.getName())
            .pageId(slot.getPage() != null ? slot.getPage().getId() : null)
            .components(componentDTOs)
            .syncStatus(slotSyncStatus.getOrDefault(slot.getSyncKey(), "UNKNOWN"))
            .build();
    }
}
