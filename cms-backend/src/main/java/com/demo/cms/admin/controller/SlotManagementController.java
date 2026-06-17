package com.demo.cms.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.demo.cms.admin.dto.CreateSlotRequest;
import com.demo.cms.admin.dto.SlotResponse;
import com.demo.cms.admin.dto.UpdateSlotRequest;
import com.demo.cms.admin.service.StorefrontCacheEvictionService;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.repository.PageRepository;
import com.demo.cms.admin.repository.SlotRepository;
import com.demo.cms.dto.ComponentDTO;
import com.demo.cms.entity.Page;
import com.demo.cms.entity.Slot;
import com.demo.cms.mapper.EntityMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cms/slots")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class SlotManagementController {

    private final SlotRepository slotRepository;
    private final PageRepository pageRepository;
    private final EntityMapper entityMapper;
    private final StorefrontCacheEvictionService storefrontCacheEvictionService;

    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<SlotResponse>> getSlotsByPage(@PathVariable Long pageId) {
        List<Slot> slots = slotRepository.findByPageId(pageId);
        List<SlotResponse> responses = slots.stream()
            .map(this::mapToSlotResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlot(@PathVariable Long id) {
        Slot slot = slotRepository.findByIdWithComponents(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
        return ResponseEntity.ok(mapToSlotResponse(slot));
    }

    @PostMapping
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        Page page = pageRepository.findById(request.getPageId())
            .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + request.getPageId()));

        Slot slot = Slot.builder()
            .code(request.getCode())
            .name(request.getName())
            .page(page)
            .build();

        Slot savedSlot = slotRepository.save(slot);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSlotResponse(savedSlot));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<SlotResponse> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSlotRequest request) {
        
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        slot.setCode(request.getCode());
        slot.setName(request.getName());

        Slot updatedSlot = slotRepository.save(slot);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.ok(mapToSlotResponse(updatedSlot));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"page", "slot"}, allEntries = true)
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        if (!slotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Slot not found with id: " + id);
        }
        slotRepository.deleteById(id);
        storefrontCacheEvictionService.evictStorefrontCaches();
        return ResponseEntity.noContent().build();
    }

    private SlotResponse mapToSlotResponse(Slot slot) {
        List<ComponentDTO> componentDTOs = slot.getComponents().stream()
            .map(entityMapper::toComponentDTO)
            .collect(Collectors.toList());

        return SlotResponse.builder()
            .id(slot.getId())
            .code(slot.getCode())
            .name(slot.getName())
            .pageId(slot.getPage().getId())
            .components(componentDTOs)
            .build();
    }
}
