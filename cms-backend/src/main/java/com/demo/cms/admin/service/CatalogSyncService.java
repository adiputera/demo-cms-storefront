package com.demo.cms.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.cms.admin.repository.CatalogRepository;
import com.demo.cms.admin.repository.ComponentRepository;
import com.demo.cms.admin.repository.PageRepository;
import com.demo.cms.admin.repository.SlotRepository;
import com.demo.cms.entity.Catalog;
import com.demo.cms.entity.CatalogVersion;
import com.demo.cms.entity.Component;
import com.demo.cms.entity.Page;
import com.demo.cms.entity.Slot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSyncService {

    private final CatalogRepository catalogRepository;
    private final PageRepository pageRepository;
    private final SlotRepository slotRepository;
    private final ComponentRepository componentRepository;

    @Transactional
    public void syncCatalog(String catalogId) {
        log.info("Starting sync for catalog: {}", catalogId);

        Catalog stagedCatalog = catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.STAGED)
            .orElseThrow(() -> new IllegalArgumentException("Staged catalog not found: " + catalogId));

        Catalog onlineCatalog = catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.ONLINE)
            .orElseGet(() -> {
                log.info("Online catalog not found for {}, creating...", catalogId);
                Catalog newCatalog = Catalog.builder()
                    .catalogId(catalogId)
                    .version(CatalogVersion.ONLINE)
                    .build();
                return catalogRepository.save(newCatalog);
            });

        // 1. Sync Pages
        List<Page> stagedPages = pageRepository.findAllByCatalog(stagedCatalog);
        for (Page stagedPage : stagedPages) {
            Page onlinePage = pageRepository.findBySlugAndCatalog(stagedPage.getSlug(), onlineCatalog)
                .orElseGet(Page::new);
            
            BeanUtils.copyProperties(stagedPage, onlinePage, "id", "catalog", "slots", "breadcrumbs", "createdAt", "updatedAt");
            onlinePage.setCatalog(onlineCatalog);
            pageRepository.save(onlinePage);
        }

        // 2. Sync Components (before Slots, so we can link them)
        List<Component> stagedComponents = componentRepository.findAllByCatalog(stagedCatalog);
        for (Component stagedComponent : stagedComponents) {
            Component onlineComponent = componentRepository.findByUidAndCatalog(stagedComponent.getUid(), onlineCatalog)
                .orElseGet(() -> {
                    try {
                        return stagedComponent.getClass().getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate component subclass: " + stagedComponent.getClass(), e);
                    }
                });
            
            BeanUtils.copyProperties(stagedComponent, onlineComponent, "id", "catalog", "createdAt", "updatedAt");
            onlineComponent.setCatalog(onlineCatalog);
            componentRepository.save(onlineComponent);
        }

        // 3. Sync Slots and their Component relationships
        List<Slot> stagedSlots = slotRepository.findAllByCatalog(stagedCatalog);
        for (Slot stagedSlot : stagedSlots) {
            Page onlinePage = pageRepository.findBySlugAndCatalog(stagedSlot.getPage().getSlug(), onlineCatalog)
                .orElseThrow(() -> new IllegalStateException("Online page not found for slot sync"));
            
            Slot onlineSlot = slotRepository.findByCodeAndPageAndCatalog(stagedSlot.getCode(), onlinePage, onlineCatalog)
                .orElseGet(Slot::new);
            
            BeanUtils.copyProperties(stagedSlot, onlineSlot, "id", "catalog", "page", "components", "createdAt", "updatedAt");
            onlineSlot.setCatalog(onlineCatalog);
            onlineSlot.setPage(onlinePage);
            
            // Sync components relation
            List<Component> onlineComponents = new ArrayList<>();
            for (Component stagedComp : stagedSlot.getComponents()) {
                Component onlineComp = componentRepository.findByUidAndCatalog(stagedComp.getUid(), onlineCatalog)
                    .orElseThrow(() -> new IllegalStateException("Online component not found for slot sync"));
                onlineComponents.add(onlineComp);
            }
            onlineSlot.setComponents(onlineComponents);
            
            slotRepository.save(onlineSlot);
        }
        
        log.info("Catalog sync completed successfully for: {}", catalogId);
    }
}
