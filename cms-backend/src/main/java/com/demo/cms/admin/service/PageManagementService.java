package com.demo.cms.admin.service;

import com.demo.cms.dto.PageDTO;
import com.demo.cms.entity.Page;
import com.demo.cms.entity.Catalog;
import com.demo.cms.admin.repository.PageRepository;
import com.demo.cms.admin.repository.CatalogRepository;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.exception.DuplicateResourceException;
import com.demo.cms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageManagementService {

    private final PageRepository pageRepository;
    private final CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final StorefrontCacheEvictionService storefrontCacheEvictionService;

    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("contentCatalog", com.demo.cms.entity.CatalogVersion.STAGED)
            .orElseGet(() -> {
                Catalog cat = new Catalog();
                cat.setCatalogId("contentCatalog");
                cat.setVersion(com.demo.cms.entity.CatalogVersion.STAGED);
                return catalogRepository.save(cat);
            });
    }

    @Transactional(readOnly = true)
    public List<PageDTO> getAllPages() {
        log.debug("Fetching all pages");
        List<Page> pages = pageRepository.findAllByCatalog(getStagedCatalog());
        return pages.stream()
                .map(page -> entityMapper.toPageDTO(page, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageDTO getPageById(Long id) {
        log.debug("Fetching page with ID: {}", id);
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", id));
        return entityMapper.toPageDTO(page, true);
    }

    @Transactional
    @CacheEvict(value = "pages", key = "#result.slug")
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
                .robotsIndex(pageDTO.getRobotsIndex())
                .robotsFollow(pageDTO.getRobotsFollow())
                .ogTitle(pageDTO.getOgTitle())
                .ogDescription(pageDTO.getOgDescription())
                .ogImage(pageDTO.getOgImage())
                .build();
        page.setCatalog(getStagedCatalog());

        Page savedPage = pageRepository.save(page);
        log.info("Page created successfully with ID: {}", savedPage.getId());
        storefrontCacheEvictionService.evictStorefrontCaches();
        return entityMapper.toPageDTO(savedPage, true);
    }

    @Transactional
    @CacheEvict(value = "pages", key = "#pageDTO.slug")
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
        page.setRobotsIndex(pageDTO.getRobotsIndex());
        page.setRobotsFollow(pageDTO.getRobotsFollow());
        page.setOgTitle(pageDTO.getOgTitle());
        page.setOgDescription(pageDTO.getOgDescription());
        page.setOgImage(pageDTO.getOgImage());

        Page updatedPage = pageRepository.save(page);
        log.info("Page updated successfully with ID: {}", updatedPage.getId());
        storefrontCacheEvictionService.evictStorefrontCaches();
        return entityMapper.toPageDTO(updatedPage, true);
    }

    @Transactional
    @CacheEvict(value = "pages", allEntries = true)
    public void deletePage(Long id) {
        log.info("Deleting page with ID: {}", id);
        
        if (!pageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Page", id);
        }

        pageRepository.deleteById(id);
        storefrontCacheEvictionService.evictStorefrontCaches();
        log.info("Page deleted successfully with ID: {}", id);
    }
}
