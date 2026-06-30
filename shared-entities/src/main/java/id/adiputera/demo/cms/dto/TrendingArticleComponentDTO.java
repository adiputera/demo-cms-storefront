package id.adiputera.demo.cms.dto;

import java.util.List;

import id.adiputera.demo.cms.entity.ComponentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TrendingArticleComponentDTO extends ComponentDTO {
    private String title;
    private List<String> articleIds;

    public TrendingArticleComponentDTO() {
        super();
        this.setType(ComponentType.TRENDING_ARTICLE.name());
    }
}
