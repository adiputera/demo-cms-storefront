package id.adiputera.demo.cms.dto;

import id.adiputera.demo.cms.entity.ComponentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Trending Article Component D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@Setter
@SuperBuilder
public class TrendingArticleComponentDTO extends ComponentDTO {
    private String title;
    private List<String> articleSlugs;

    public TrendingArticleComponentDTO() {
        super();
        this.setType(ComponentType.TRENDING_ARTICLE.name());
    }
}
