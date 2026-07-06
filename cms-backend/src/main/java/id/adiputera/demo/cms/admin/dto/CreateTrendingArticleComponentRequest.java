package id.adiputera.demo.cms.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Create Trending Article Component Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@Setter
public class CreateTrendingArticleComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> articleIds;
}
