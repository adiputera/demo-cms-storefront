package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.CreateSlotRequest;
import id.adiputera.demo.cms.admin.dto.ReorderSlotRequest;
import id.adiputera.demo.cms.admin.dto.SlotResponse;
import id.adiputera.demo.cms.admin.dto.UpdateSlotRequest;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.CatalogRepository;
import id.adiputera.demo.cms.admin.repository.PageRepository;
import id.adiputera.demo.cms.admin.repository.SlotRepository;
import id.adiputera.demo.cms.dto.ComponentDTO;
import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogVersion;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.Page;
import id.adiputera.demo.cms.entity.Slot;
import id.adiputera.demo.cms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service handling transactional business logic for CMS slots.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotManagementService {

    private final SlotRepository slotRepository;
    private final PageRepository pageRepository;
    private final CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final CatalogSyncService catalogSyncService;

    /**
     * Retrieves or creates the STAGED catalog.
     *
     * @return The STAGED catalog entity.
     */
    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", CatalogVersion.STAGED)
            .orElseGet(() -> {
                Catalog cat = new Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(CatalogVersion.STAGED);
                return catalogRepository.save(cat);
            });
    }

    /**
     * Retrieves all slots across all pages with sync status.
     *
     * @return A list of slot responses.
     */
    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots() {
        List<Slot> slots = slotRepository.findAll();
        Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(slots, Slot.class);

        List<Component> allComponents = slots.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> s.getComponents() != null ? s.getComponents().stream().filter(Objects::nonNull) : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
        Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(allComponents, Component.class);

        return slots.stream()
                .filter(Objects::nonNull)
                .map(slot -> mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all slots belonging to a specific page.
     *
     * @param pageId The ID of the page.
     * @return A list of slot responses for the page.
     */
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByPage(Long pageId) {
        List<Slot> slots = slotRepository.findByPageId(pageId);
        Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(slots, Slot.class);

        List<Component> allComponents = slots.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> s.getComponents() != null ? s.getComponents().stream().filter(Objects::nonNull) : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
        Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(allComponents, Component.class);

        return slots.stream()
                .filter(Objects::nonNull)
                .map(slot -> mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a slot by its ID with components.
     *
     * @param id The slot ID.
     * @return The slot response.
     */
    @Transactional(readOnly = true)
    public SlotResponse getSlot(Long id) {
        Slot slot = slotRepository.findByIdWithComponents(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        Map<String, String> slotSyncStatus = catalogSyncService.calculateSyncStatus(List.of(slot), Slot.class);
        Map<String, String> componentSyncStatus = catalogSyncService.calculateSyncStatus(
                new ArrayList<>(slot.getComponents()), Component.class);

        return mapToSlotResponse(slot, slotSyncStatus, componentSyncStatus);
    }

    /**
     * Creates a new slot assigned to a page.
     *
     * @param request The create slot request containing code, name, and page ID.
     * @return The created slot response.
     */
    @Transactional
    public SlotResponse createSlot(CreateSlotRequest request) {
        Page page = pageRepository.findById(request.getPageId())
            .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + request.getPageId()));

        Slot slot = Slot.builder()
            .code(request.getCode())
            .name(request.getName())
            .page(page)
            .build();
        slot.setCatalog(getStagedCatalog());

        Slot savedSlot = slotRepository.save(slot);

        int index = request.getSortOrder() != null ? request.getSortOrder() : page.getSlots().size();
        if (index > page.getSlots().size()) index = page.getSlots().size();
        page.getSlots().add(index, savedSlot);
        pageRepository.save(page);

        return mapToSlotResponse(savedSlot, new HashMap<>(), new HashMap<>());
    }

    /**
     * Updates an existing slot.
     *
     * @param id The ID of the slot to update.
     * @param request The update slot request containing the new code and name.
     * @return The updated slot response.
     */
    @Transactional
    public SlotResponse updateSlot(Long id, UpdateSlotRequest request) {
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        slot.setCode(request.getCode());
        slot.setName(request.getName());

        Slot updatedSlot = slotRepository.save(slot);
        return mapToSlotResponse(updatedSlot, new HashMap<>(), new HashMap<>());
    }

    /**
     * Reorders a slot within its assigned page.
     *
     * @param id The ID of the slot to reorder.
     * @param request The request specifying the new sort order index.
     * @return true if reordered, false if indices invalid.
     */
    @Transactional
    public boolean reorderSlot(Long id, ReorderSlotRequest request) {
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
        Page page = slot.getPage();

        List<Slot> slots = page.getSlots();
        int currentIndex = slots.indexOf(slot);
        int newIndex = request.getSortOrder();

        if (currentIndex == -1 || newIndex < 0 || newIndex >= slots.size()) {
            return false;
        }

        if (currentIndex != newIndex) {
            slots.remove(currentIndex);
            slots.add(newIndex, slot);
            pageRepository.save(page);
        }
        return true;
    }

    /**
     * Deletes a slot by its ID.
     *
     * @param id The ID of the slot to delete.
     */
    @Transactional
    public void deleteSlot(Long id) {
        if (!slotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Slot not found with id: " + id);
        }
        slotRepository.deleteById(id);
    }

    /**
     * Maps a Slot entity to a SlotResponse DTO using the provided sync status maps.
     *
     * @param slot The slot entity to map.
     * @param slotSyncStatus A map of slot sync keys to their synchronization status.
     * @param componentSyncStatus A map of component sync keys to their synchronization status.
     * @return The mapped SlotResponse.
     */
    private SlotResponse mapToSlotResponse(Slot slot, Map<String, String> slotSyncStatus, Map<String, String> componentSyncStatus) {
        if (slot == null) return null;
        List<ComponentDTO> componentDTOs = new ArrayList<>();
        if (slot.getComponents() != null) {
            int sortOrder = 0;
            for (Component component : slot.getComponents()) {
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
