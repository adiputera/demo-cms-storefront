package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.exception.DuplicateResourceException;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.CatalogRepository;
import id.adiputera.demo.cms.admin.repository.PageRepository;
import id.adiputera.demo.cms.dto.PageDTO;
import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Page;
import id.adiputera.demo.cms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Page Management Service class.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PageManagementService {

    private final PageRepository pageRepository;
    private final CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final CatalogSyncService catalogSyncService;

    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
            .orElseGet(() -> {
                Catalog cat = new Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(id.adiputera.demo.cms.entity.CatalogVersion.STAGED);
                return catalogRepository.save(cat);
            });
    }

    @Transactional
    public List<PageDTO> getAllPages() {
        log.debug("Fetching all pages");
        List<Page> pages = pageRepository.findAllByCatalog(getStagedCatalog());
        
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(pages, Page.class);
        
        return pages.stream()
                .map(page -> {
                    PageDTO dto = entityMapper.toPageDTO(page, false);
                    dto.setSyncStatus(syncStatusMap.getOrDefault(page.getSyncKey(), "UNKNOWN"));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PageDTO getPageById(Long id) {
        log.debug("Fetching page with ID: {}", id);
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", id));
        
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(List.of(page), Page.class);
        PageDTO dto = entityMapper.toPageDTO(page, true);
        dto.setSyncStatus(syncStatusMap.getOrDefault(page.getSyncKey(), "UNKNOWN"));
        return dto;
    }

    @Transactional
    public PageDTO createPage(PageDTO pageDTO) {
        log.info("Creating new page with slug: {}", pageDTO.getSlug());
        
        if (pageRepository.existsBySlugAndCatalog(pageDTO.getSlug(), getStagedCatalog())) {
            throw new DuplicateResourceException("Page", "slug", pageDTO.getSlug());
        }

        Page page = Page.builder()
                .slug(pageDTO.getSlug())
                .title(pageDTO.getTitle())
                .breadcrumbTitle(pageDTO.getBreadcrumbTitle())
                .metaTitle(pageDTO.getMetaTitle())
                .metaDescription(pageDTO.getMetaDescription())
                .metaKeywords(pageDTO.getMetaKeywords())
                .canonicalUrl(pageDTO.getCanonicalUrl())
                .robotsIndex(pageDTO.getRobotsIndex() != null ? pageDTO.getRobotsIndex() : true)
                .robotsFollow(pageDTO.getRobotsFollow() != null ? pageDTO.getRobotsFollow() : true)
                .ogTitle(pageDTO.getOgTitle())
                .ogDescription(pageDTO.getOgDescription())
                .ogImage(pageDTO.getOgImage())
                .build();
        page.setCatalog(getStagedCatalog());

        Page savedPage = pageRepository.save(page);
        log.info("Page created successfully with ID: {}", savedPage.getId());
        return entityMapper.toPageDTO(savedPage, true);
    }

    @Transactional
    public PageDTO updatePage(Long id, PageDTO pageDTO) {
        log.info("Updating page with ID: {}", id);
        
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", id));

        // Check if slug is being changed and if new slug already exists
        if (!page.getSlug().equals(pageDTO.getSlug()) && 
            pageRepository.existsBySlugAndCatalog(pageDTO.getSlug(), getStagedCatalog())) {
            throw new DuplicateResourceException("Page", "slug", pageDTO.getSlug());
        }

        page.setSlug(pageDTO.getSlug());
        page.setTitle(pageDTO.getTitle());
        page.setBreadcrumbTitle(pageDTO.getBreadcrumbTitle());
        page.setMetaTitle(pageDTO.getMetaTitle());
        page.setMetaDescription(pageDTO.getMetaDescription());
        page.setMetaKeywords(pageDTO.getMetaKeywords());
        page.setCanonicalUrl(pageDTO.getCanonicalUrl());
        page.setRobotsIndex(pageDTO.getRobotsIndex() != null ? pageDTO.getRobotsIndex() : true);
        page.setRobotsFollow(pageDTO.getRobotsFollow() != null ? pageDTO.getRobotsFollow() : true);
        page.setOgTitle(pageDTO.getOgTitle());
        page.setOgDescription(pageDTO.getOgDescription());
        page.setOgImage(pageDTO.getOgImage());

        Page updatedPage = pageRepository.save(page);
        log.info("Page updated successfully with ID: {}", updatedPage.getId());
        return entityMapper.toPageDTO(updatedPage, true);
    }

    @Transactional
    public void deletePage(Long id) {
        log.info("Deleting page with ID: {}", id);
        
        if (!pageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Page", id);
        }

        pageRepository.deleteById(id);
        log.info("Page deleted successfully with ID: {}", id);
    }
}
