package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Page Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {
    
    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 255)
    private String breadcrumbTitle;
    
    @Size(max = 255)
    private String metaTitle;
    
    @Size(max = 500)
    private String metaDescription;
    
    @Size(max = 500)
    private String metaKeywords;
    
    @Size(max = 500)
    private String canonicalUrl;
    
    private Boolean robotsIndex;
    
    private Boolean robotsFollow;
    
    @Size(max = 255)
    private String ogTitle;
    
    @Size(max = 500)
    private String ogDescription;
    
    @Size(max = 500)
    private String ogImage;
}
