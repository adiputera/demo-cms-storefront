package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create Article Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
public class CreateArticleRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Slug is required")
    private String slug;
    private String body;
}
