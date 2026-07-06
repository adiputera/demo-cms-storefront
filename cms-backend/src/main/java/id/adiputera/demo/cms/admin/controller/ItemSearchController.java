package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchRequest;
import id.adiputera.demo.cms.admin.service.ItemSearchService;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Item Search Controller class.
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
        var criteria = request.getCriteria() != null ? request.getCriteria() : List.<id.adiputera.demo.cms.admin.dto.SearchCriteria>of();
        List<ItemSearchResultDTO> results = itemSearchService.searchItems(type, criteria);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
