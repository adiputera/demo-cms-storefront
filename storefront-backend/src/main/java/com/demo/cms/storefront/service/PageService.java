package com.demo.cms.storefront.service;

import com.demo.cms.dto.PageDTO;
import com.demo.cms.entity.Page;
import com.demo.cms.storefront.exception.ResourceNotFoundException;
import com.demo.cms.mapper.EntityMapper;
import com.demo.cms.storefront.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;
    private final EntityMapper entityMapper;

    @Cacheable(value = "pages", key = "#slug")
    @Transactional(readOnly = true)
    public PageDTO getPageBySlug(String slug) {
        log.debug("Fetching page with slug: {}", slug);

        Page page = pageRepository.findBySlugWithRelations(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page", slug));

        return entityMapper.toPageDTO(page, true);
    }
}
