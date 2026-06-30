package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArticleRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Slug is required")
    private String slug;
    private String body;
}
