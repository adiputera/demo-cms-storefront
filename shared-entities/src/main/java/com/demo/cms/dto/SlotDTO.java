package com.demo.cms.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {
    private Long id;
    private String code;
    private String name;
    private List<ComponentDTO> components;
}
