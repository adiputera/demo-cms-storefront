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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cms/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final PageRepository pageRepository;
    private final ProductRepository productRepository;
    private final ComponentRepository componentRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        long totalPages = pageRepository.count();
        long totalProducts = productRepository.count();
        long totalComponents = componentRepository.count();

        return ResponseEntity.ok(Map.of(
            "totalPages", totalPages,
            "totalProducts", totalProducts,
            "totalComponents", totalComponents
        ));
    }
}
