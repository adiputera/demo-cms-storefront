package com.demo.cms.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product code is required")
    private String code;

    @NotBlank(message = "Product name is required")
    private String name;

    private String imageUrl;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String description;
    private String syncStatus;
    private Integer syncVersion;
}
