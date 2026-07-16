package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ComponentSchema;
import id.adiputera.demo.cms.admin.dto.ComponentTypeInfo;
import id.adiputera.demo.cms.admin.dto.CreateComponentRequest;
import id.adiputera.demo.cms.admin.dto.ReorderComponentRequest;
import id.adiputera.demo.cms.admin.service.ComponentManagementService;
import id.adiputera.demo.cms.dto.ComponentDTO;
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

    private final ComponentManagementService componentManagementService;

    /**
     * Retrieves all components with synchronization status.
     *
     * @return A list of component DTOs wrapped in ResponseEntity.
     */
    @GetMapping
    public ResponseEntity<List<ComponentDTO>> getAllComponents() {
        return ResponseEntity.ok(componentManagementService.getAllComponents());
    }

    /**
     * Creates a new component within a specified slot.
     *
     * @param request The component creation request.
     * @return The created component DTO wrapped in ResponseEntity.
     */
    @PostMapping
    public ResponseEntity<ComponentDTO> createComponent(@Valid @RequestBody CreateComponentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentManagementService.createComponent(request));
    }

    /**
     * Links an existing component to a slot at an optional sort order.
     *
     * @param slotId The target slot ID.
     * @param componentId The component ID to link.
     * @param payload Map containing sortOrder if applicable.
     * @return Empty ResponseEntity indicating completion.
     */
    @PostMapping("/slots/{slotId}/components/{componentId}")
    public ResponseEntity<Void> linkComponent(@PathVariable Long slotId, @PathVariable Long componentId, @RequestBody Map<String, Integer> payload) {
        componentManagementService.linkComponent(slotId, componentId, payload);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an existing component's data.
     *
     * @param id The component ID to update.
     * @param request The component update request.
     * @return The updated component DTO wrapped in ResponseEntity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComponentDTO> updateComponent(
            @PathVariable Long id,
            @Valid @RequestBody CreateComponentRequest request) {
        return ResponseEntity.ok(componentManagementService.updateComponent(id, request));
    }

    /**
     * Reorders a component inside a specific slot.
     *
     * @param slotId The ID of the slot.
     * @param id The component ID.
     * @param request The reorder request with target index.
     * @return Empty ResponseEntity indicating completion.
     */
    @PutMapping("/slots/{slotId}/components/{id}/reorder")
    public ResponseEntity<Void> reorderComponent(
            @PathVariable Long slotId,
            @PathVariable Long id,
            @Valid @RequestBody ReorderComponentRequest request) {
        componentManagementService.reorderComponent(slotId, id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes a component from a specific slot without deleting the component itself.
     *
     * @param slotId The ID of the slot.
     * @param id The component ID to remove from slot.
     * @return Empty ResponseEntity indicating completion.
     */
    @DeleteMapping("/slots/{slotId}/components/{id}")
    public ResponseEntity<Void> removeComponentFromSlot(@PathVariable Long slotId, @PathVariable Long id) {
        componentManagementService.removeComponentFromSlot(slotId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently deletes a component by ID.
     *
     * @param id The component ID.
     * @return Empty ResponseEntity indicating completion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        componentManagementService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves metadata info for all supported component types.
     *
     * @return List of component type metadata wrapped in ResponseEntity.
     */
    @GetMapping("/types")
    public ResponseEntity<List<ComponentTypeInfo>> getComponentTypes() {
        return ResponseEntity.ok(componentManagementService.getComponentTypes());
    }

    /**
     * Retrieves schema definition for a specific component type.
     *
     * @param type The component type identifier.
     * @return The component schema wrapped in ResponseEntity.
     */
    @GetMapping("/types/{type}/schema")
    public ResponseEntity<ComponentSchema> getComponentSchema(@PathVariable String type) {
        return ResponseEntity.ok(componentManagementService.getComponentSchema(type));
    }
}
