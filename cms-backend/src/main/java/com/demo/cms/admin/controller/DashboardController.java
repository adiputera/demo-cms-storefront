package com.demo.cms.admin.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.demo.cms.admin.repository.PageRepository;
import com.demo.cms.admin.repository.ProductRepository;
import com.demo.cms.admin.repository.ComponentRepository;
import com.demo.cms.admin.repository.CatalogRepository;
import com.demo.cms.entity.Catalog;
import com.demo.cms.entity.CatalogVersion;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cms/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final PageRepository pageRepository;
    private final ProductRepository productRepository;
    private final ComponentRepository componentRepository;
    private final CatalogRepository catalogRepository;

    private Catalog getStagedCatalog(String catalogId) {
        return catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.STAGED).orElse(null);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Catalog stagedContentCatalog = getStagedCatalog("contentCatalog");
        Catalog stagedProductCatalog = getStagedCatalog("productCatalog");

        long totalPages = stagedContentCatalog != null ? pageRepository.countByCatalog(stagedContentCatalog) : 0;
        long totalProducts = stagedProductCatalog != null ? productRepository.countByCatalog(stagedProductCatalog) : 0;
        long totalComponents = stagedContentCatalog != null ? componentRepository.countByCatalog(stagedContentCatalog) : 0;

        return ResponseEntity.ok(Map.of(
            "totalPages", totalPages,
            "totalProducts", totalProducts,
            "totalComponents", totalComponents
        ));
    }
}
