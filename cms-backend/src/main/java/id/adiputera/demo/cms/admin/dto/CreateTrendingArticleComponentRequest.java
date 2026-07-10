package id.adiputera.demo.cms.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Create Trending Article Component Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@Setter
public class CreateTrendingArticleComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> articleSlugs;
}
