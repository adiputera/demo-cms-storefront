package com.demo.cms.storefront.service;

import com.demo.cms.dto.SlotDTO;
import com.demo.cms.entity.Slot;
import com.demo.cms.mapper.EntityMapper;
import com.demo.cms.storefront.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;
    private final EntityMapper entityMapper;

    @Cacheable(value = "slots", key = "#slotIds.toString()")
    @Transactional(readOnly = true)
    public List<SlotDTO> getSlotsByIds(List<Long> slotIds) {
        log.debug("Fetching slots with IDs: {}", slotIds);

        if (slotIds == null || slotIds.isEmpty()) {
            return List.of();
        }

        List<Slot> slots = slotRepository.findByIdInWithComponents(slotIds);

        return slots.stream()
                .map(entityMapper::toSlotDTOWithComponents)
                .collect(Collectors.toList());
    }
}
