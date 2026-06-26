package com.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateQuickMenuComponentRequest extends CreateComponentRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    @NotBlank(message = "URL is required")
    private String url;
}
