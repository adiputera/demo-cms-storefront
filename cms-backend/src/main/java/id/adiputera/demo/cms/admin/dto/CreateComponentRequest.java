package id.adiputera.demo.cms.admin.dto;

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
    @JsonSubTypes.Type(value = CreateProductDetailComponentRequest.class, name = "PRODUCT_DETAIL"),
    @JsonSubTypes.Type(value = CreateLatestArticleComponentRequest.class, name = "LATEST_ARTICLE"),
    @JsonSubTypes.Type(value = CreateTrendingArticleComponentRequest.class, name = "TRENDING_ARTICLE"),
    @JsonSubTypes.Type(value = CreateLatestEventComponentRequest.class, name = "LATEST_EVENT"),
    @JsonSubTypes.Type(value = CreateTopEventComponentRequest.class, name = "TOP_EVENT")
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


