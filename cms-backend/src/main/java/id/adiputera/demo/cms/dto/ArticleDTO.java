package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Article D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    private Long id;
    private String title;
    private String slug;
    private String body;
    private String syncStatus;
}
