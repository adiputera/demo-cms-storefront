package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.dto.CmsRowDTO;
import id.adiputera.demo.cms.admin.dto.ItemMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchRequest;
import id.adiputera.demo.cms.admin.dto.ModelInfoDTO;
import id.adiputera.demo.cms.admin.service.CmsCrudService;
import id.adiputera.demo.cms.admin.service.ItemSearchService;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
 * Item Search Controller class providing administrative APIs for metadata exploration, searching, and generic CRUD.
 *
 * @author Yusuf F. Adiputera
 */
@RestController
@RequestMapping("/api/cms/items")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ItemSearchController {

    private final ItemSearchService itemSearchService;
    private final CmsCrudService cmsCrudService;

    /**
     * Gets all registered domain model types.
     *
     * @return Response entity containing a list of registered CMS types.
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<ModelInfoDTO>>> getAvailableTypes() {
        log.info("GET /api/cms/items/types");
        List<ModelInfoDTO> types = itemSearchService.getAvailableTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    /**
     * Gets the unified metadata for a given model type, containing searchable fields and column definitions.
     *
     * @param type The lower-case entity type.
     * @return Response entity containing the unified metadata DTO.
     */
    @GetMapping("/{type}/metadata")
    public ResponseEntity<ApiResponse<ItemMetadataDTO>> getUnifiedMetadata(@PathVariable("type") String type) {
        log.info("GET /api/cms/items/{}/metadata", type);
        ItemMetadataDTO metadata = itemSearchService.getUnifiedMetadata(type);
        return ResponseEntity.ok(ApiResponse.success(metadata));
    }

    /**
     * Gets the search metadata for a given item type.
     *
     * @param type The item type string.
     * @return The search metadata DTO.
     */
    @GetMapping("/{type}/search-metadata")
    public ResponseEntity<ApiResponse<ItemSearchMetadataDTO>> getSearchMetadata(@PathVariable("type") String type) {
        log.info("GET /api/cms/items/{}/search-metadata", type);
        ItemSearchMetadataDTO metadata = itemSearchService.getSearchMetadata(type);
        return ResponseEntity.ok(ApiResponse.success(metadata));
    }

    /**
     * Searches items of a type using criteria and returns standard picker result DTOs.
     *
     * @param type The lower-case entity type.
     * @param request The search request wrapper.
     * @return Response entity containing search results.
     */
    @PostMapping("/{type}/search")
    public ResponseEntity<ApiResponse<List<ItemSearchResultDTO>>> searchItems(
            @PathVariable("type") String type,
            @RequestBody ItemSearchRequest request) {
        log.info("POST /api/cms/items/{}/search with criteria: {}", type, request.getCriteria());
        var criteria = request.getCriteria() != null ? request.getCriteria() : List.<id.adiputera.demo.cms.admin.dto.SearchCriteria>of();
        List<ItemSearchResultDTO> results = itemSearchService.searchItems(type, criteria);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Searches items of a type using criteria and returns dynamic generic row DTOs.
     *
     * @param type The lower-case entity type.
     * @param request The search request wrapper.
     * @return Response entity containing generic row search results.
     */
    @PostMapping("/{type}/list")
    public ResponseEntity<ApiResponse<List<CmsRowDTO>>> searchItemsForList(
            @PathVariable("type") String type,
            @RequestBody ItemSearchRequest request) {
        log.info("POST /api/cms/items/{}/list with criteria: {}", type, request.getCriteria());
        var criteria = request.getCriteria() != null ? request.getCriteria() : List.<id.adiputera.demo.cms.admin.dto.SearchCriteria>of();
        List<CmsRowDTO> results = itemSearchService.searchItemsForList(type, criteria);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Retrieves a single entity's tabular representation by type and primary identifier.
     *
     * @param type The lower-case entity type.
     * @param id The database primary identifier.
     * @return Response entity containing the tabular row representation.
     */
    @GetMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<CmsRowDTO>> getEntityById(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        log.info("GET /api/cms/items/{}/{}", type, id);
        CmsRowDTO result = cmsCrudService.getEntityById(type, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Creates a new entity instance dynamically based on type and input fields.
     *
     * @param type The lower-case entity type.
     * @param payload The raw JSON fields input map.
     * @return Response entity containing the persisted entity's tabular row representation.
     */
    @PostMapping("/{type}")
    public ResponseEntity<ApiResponse<CmsRowDTO>> createEntity(
            @PathVariable("type") String type,
            @RequestBody Map<String, Object> payload) {
        log.info("POST /api/cms/items/{} with payload: {}", type, payload);
        CmsRowDTO result = cmsCrudService.createEntity(type, payload);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Updates an existing entity dynamically based on type, identifier, and input fields.
     *
     * @param type The lower-case entity type.
     * @param id The database primary identifier.
     * @param payload The raw JSON fields update input map.
     * @return Response entity containing the updated entity's tabular row representation.
     */
    @PutMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<CmsRowDTO>> updateEntity(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> payload) {
        log.info("PUT /api/cms/items/{}/{} with payload: {}", type, id, payload);
        CmsRowDTO result = cmsCrudService.updateEntity(type, id, payload);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Deletes an entity instance by type and identifier.
     *
     * @param type The lower-case entity type.
     * @param id The database primary identifier.
     * @return Response entity indicating success status.
     */
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        log.info("DELETE /api/cms/items/{}/{}", type, id);
        cmsCrudService.deleteEntity(type, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
