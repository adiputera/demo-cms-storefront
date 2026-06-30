package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchRequest;
import id.adiputera.demo.cms.admin.dto.ItemSearchResultDTO;
import id.adiputera.demo.cms.admin.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cms/items")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ItemSearchController {

    private final ItemSearchService itemSearchService;

    @GetMapping("/{type}/search-metadata")
    public ResponseEntity<ApiResponse<ItemSearchMetadataDTO>> getSearchMetadata(@PathVariable("type") String type) {
        log.info("GET /api/cms/items/{}/search-metadata", type);
        ItemSearchMetadataDTO metadata = itemSearchService.getSearchMetadata(type);
        return ResponseEntity.ok(ApiResponse.success(metadata));
    }

    @PostMapping("/{type}/search")
    public ResponseEntity<ApiResponse<List<ItemSearchResultDTO>>> searchItems(
            @PathVariable("type") String type,
            @RequestBody ItemSearchRequest request) {
        log.info("POST /api/cms/items/{}/search with criteria: {}", type, request.getCriteria());
        Map<String, String> criteria = request.getCriteria() != null ? request.getCriteria() : Map.of();
        List<ItemSearchResultDTO> results = itemSearchService.searchItems(type, criteria);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
