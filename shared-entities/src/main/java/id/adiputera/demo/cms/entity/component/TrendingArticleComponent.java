package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Trending Article Component class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "trending_article_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Trending Articles", description = "List of trending articles")
public class TrendingArticleComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Section Title", type = "string", required = true, placeholder = "Trending Now")
    private String title;

    @Column(name = "article_ids", columnDefinition = "TEXT")
    @CmsField(displayName = "Articles", type = "multiple_items:article", required = true, placeholder = "Select articles...")
    private String articleIds;

    @Override
    public ComponentType getType() {
        // Wait, I need to add TRENDING_ARTICLE to ComponentType
        return ComponentType.valueOf("TRENDING_ARTICLE"); // We will need to add it to the enum
    }
}
