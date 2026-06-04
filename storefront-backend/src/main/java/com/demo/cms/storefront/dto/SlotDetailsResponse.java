package com.demo.cms.storefront.dto;

import java.util.List;

import com.demo.cms.dto.SlotDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDetailsResponse {
    private List<SlotDTO> slots;
}
