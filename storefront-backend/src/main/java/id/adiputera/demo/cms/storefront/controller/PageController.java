package id.adiputera.demo.cms.storefront.controller;

import id.adiputera.demo.cms.dto.PageDTO;
import id.adiputera.demo.cms.storefront.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PageController {

    private final PageService pageService;

    @GetMapping("/{slug}")
    public ResponseEntity<PageDTO> getPageBySlug(@PathVariable("slug") String slug) {
        log.info("GET /api/pages/{}", slug);

        // Normalize slug to match database format (with leading slash)
        String normalizedSlug = slug.isEmpty() || slug.equals("index") ? "/" : 
            slug.startsWith("/") ? slug : "/" + slug;

        PageDTO page = pageService.getPageBySlug(normalizedSlug);
        return ResponseEntity.ok(page);
    }
}
