package com.demo.cms.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ParagraphComponentDTO.class, name = "PARAGRAPH"),
    @JsonSubTypes.Type(value = BannerComponentDTO.class, name = "BANNER"),
    @JsonSubTypes.Type(value = ProductCarouselComponentDTO.class, name = "PRODUCT_CAROUSEL"),
    @JsonSubTypes.Type(value = NavigationComponentDTO.class, name = "NAVIGATION"),
    @JsonSubTypes.Type(value = QuickMenuComponentDTO.class, name = "QUICK_MENU")
})
public abstract class ComponentDTO {

    private Long id;

    @NotBlank(message = "UID is required")
    private String uid;

    @NotBlank(message = "Component name is required")
    private String name;

    @NotNull(message = "Component type is required")
    private String type;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;
}
