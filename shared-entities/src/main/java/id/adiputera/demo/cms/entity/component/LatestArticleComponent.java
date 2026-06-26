package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "latest_article_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Latest Articles", description = "Displays a list of the latest articles")
public class LatestArticleComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Title", type = "string", required = true, placeholder = "e.g., Latest News")
    private String title;

    @Min(1)
    @Max(20)
    @Column(name = "article_count")
    @CmsField(displayName = "Number of Articles", type = "number", required = true, placeholder = "e.g., 5")
    private Integer articleCount;

    @Override
    public ComponentType getType() {
        return ComponentType.LATEST_ARTICLE;
    }
}
