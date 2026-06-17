package com.demo.cms.admin.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateBannerComponentRequest.class, name = "BANNER"),
    @JsonSubTypes.Type(value = CreateParagraphComponentRequest.class, name = "PARAGRAPH"),
    @JsonSubTypes.Type(value = CreateProductCarouselComponentRequest.class, name = "PRODUCT_CAROUSEL"),
    @JsonSubTypes.Type(value = CreateNavigationComponentRequest.class, name = "NAVIGATION"),
    @JsonSubTypes.Type(value = CreateQuickMenuComponentRequest.class, name = "QUICK_MENU"),
    @JsonSubTypes.Type(value = CreateProductDetailComponentRequest.class, name = "PRODUCT_DETAIL")
})
public abstract class CreateComponentRequest {
    
    @NotBlank(message = "UID is required")
    @Size(max = 100, message = "UID must be at most 100 characters")
    private String uid;
    
    @NotBlank(message = "Component name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;
    
    @NotBlank(message = "Component type is required")
    private String type;
    
    private Integer sortOrder;
    
    private Long slotId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateBannerComponentRequest extends CreateComponentRequest {
    private String imageUrl;
    private String altText;
    private String title;
    private String subtitle;
    private String ctaText;
    private String ctaUrl;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateParagraphComponentRequest extends CreateComponentRequest {
    private String title;
    private String content;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateProductCarouselComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> productCodes;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateNavigationComponentRequest extends CreateComponentRequest {
    private String displayText;
    private String url;
    private String icon;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateQuickMenuComponentRequest extends CreateComponentRequest {
    private String title;
    private String imageUrl;
    private String url;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateProductDetailComponentRequest extends CreateComponentRequest {
    private String title;
    private Boolean showPrice;
    private Boolean showDescription;
}
