package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.dto.CreatePageRequest;
import id.adiputera.demo.cms.admin.service.PageManagementService;
import id.adiputera.demo.cms.dto.PageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms/pages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PageManagementController {

    private final PageManagementService pageManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PageDTO>>> getAllPages() {
        log.info("GET /api/cms/pages");
        List<PageDTO> pages = pageManagementService.getAllPages();
        return ResponseEntity.ok(ApiResponse.success(pages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PageDTO>> getPageById(@PathVariable("id") Long id) {
        log.info("GET /api/cms/pages/{}", id);
        PageDTO page = pageManagementService.getPageById(id);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PageDTO>> createPage(@Valid @RequestBody CreatePageRequest request) {
        log.info("POST /api/cms/pages - Creating page with slug: {}", request.getSlug());
        
        PageDTO pageDTO = PageDTO.builder()
                .slug(request.getSlug())
                .title(request.getTitle())
                .breadcrumbTitle(request.getBreadcrumbTitle())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .canonicalUrl(request.getCanonicalUrl())
                .robotsIndex(request.getRobotsIndex())
                .robotsFollow(request.getRobotsFollow())
                .ogTitle(request.getOgTitle())
                .ogDescription(request.getOgDescription())
                .ogImage(request.getOgImage())
                .build();

        PageDTO createdPage = pageManagementService.createPage(pageDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Page created successfully", createdPage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PageDTO>> updatePage(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreatePageRequest request) {
        log.info("PUT /api/cms/pages/{}", id);
        
        PageDTO pageDTO = PageDTO.builder()
                .slug(request.getSlug())
                .title(request.getTitle())
                .breadcrumbTitle(request.getBreadcrumbTitle())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .canonicalUrl(request.getCanonicalUrl())
                .robotsIndex(request.getRobotsIndex())
                .robotsFollow(request.getRobotsFollow())
                .ogTitle(request.getOgTitle())
                .ogDescription(request.getOgDescription())
                .ogImage(request.getOgImage())
                .build();

        PageDTO updatedPage = pageManagementService.updatePage(id, pageDTO);
        return ResponseEntity.ok(ApiResponse.success("Page updated successfully", updatedPage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePage(@PathVariable("id") Long id) {
        log.info("DELETE /api/cms/pages/{}", id);
        pageManagementService.deletePage(id);
        return ResponseEntity.ok(ApiResponse.success("Page deleted successfully", null));
    }
}
