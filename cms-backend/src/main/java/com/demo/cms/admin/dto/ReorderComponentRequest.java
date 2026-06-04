package com.demo.cms.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderComponentRequest {
    
    @NotNull(message = "New sort order is required")
    private Integer sortOrder;
}
