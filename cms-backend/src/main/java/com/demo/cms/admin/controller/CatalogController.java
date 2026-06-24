package com.demo.cms.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.cms.admin.service.CatalogSyncService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogSyncService catalogSyncService;

    @PostMapping("/{catalogId}")
    public ResponseEntity<Void> syncCatalog(@PathVariable String catalogId) {
        catalogSyncService.syncCatalog(catalogId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/item/{entityType}/{itemId}")
    public ResponseEntity<Void> syncSingleItem(@PathVariable String entityType, @PathVariable Long itemId) {
        catalogSyncService.syncSingleItem(entityType, itemId);
        return ResponseEntity.ok().build();
    }
}
