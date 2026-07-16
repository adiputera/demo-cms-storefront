package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.CreateSlotRequest;
import id.adiputera.demo.cms.admin.dto.ReorderSlotRequest;
import id.adiputera.demo.cms.admin.dto.SlotResponse;
import id.adiputera.demo.cms.admin.dto.UpdateSlotRequest;
import id.adiputera.demo.cms.admin.service.SlotManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final SlotManagementService slotManagementService;

    /**
     * Retrieves all slots across all pages.
     *
     * @return A list of all slot responses wrapped in ResponseEntity.
     */
    @GetMapping
    public ResponseEntity<List<SlotResponse>> getAllSlots() {
        return ResponseEntity.ok(slotManagementService.getAllSlots());
    }

    /**
     * Retrieves all slots belonging to a specific page.
     *
     * @param pageId The ID of the page.
     * @return A list of slot responses for the page wrapped in ResponseEntity.
     */
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<SlotResponse>> getSlotsByPage(@PathVariable Long pageId) {
        return ResponseEntity.ok(slotManagementService.getSlotsByPage(pageId));
    }

    /**
     * Retrieves a slot by its ID.
     *
     * @param id The slot ID.
     * @return The slot response wrapped in ResponseEntity.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlot(@PathVariable Long id) {
        return ResponseEntity.ok(slotManagementService.getSlot(id));
    }

    /**
     * Creates a new slot.
     *
     * @param request The create slot request containing code, name, and page ID.
     * @return The created slot response wrapped in ResponseEntity.
     */
    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotManagementService.createSlot(request));
    }

    /**
     * Updates an existing slot.
     *
     * @param id The ID of the slot to update.
     * @param request The update slot request containing the new code and name.
     * @return The updated slot response wrapped in ResponseEntity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SlotResponse> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSlotRequest request) {
        return ResponseEntity.ok(slotManagementService.updateSlot(id, request));
    }

    /**
     * Reorders a slot within its assigned page.
     *
     * @param id The ID of the slot to reorder.
     * @param request The request specifying the new sort order index.
     * @return A response entity with no content or bad request if invalid index.
     */
    @PutMapping("/{id}/reorder")
    public ResponseEntity<Void> reorderSlot(
            @PathVariable Long id,
            @Valid @RequestBody ReorderSlotRequest request) {
        if (!slotManagementService.reorderSlot(id, request)) {
            return ResponseEntity.badRequest().build();
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
        slotManagementService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
