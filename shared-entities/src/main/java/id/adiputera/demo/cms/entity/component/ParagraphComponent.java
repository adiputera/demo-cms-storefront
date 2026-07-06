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
 * Paragraph Component class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "paragraph_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Paragraph Content", description = "Rich text content block with optional title")
public class ParagraphComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Title (Optional)", type = "string", required = false, placeholder = "Section Title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    @CmsField(displayName = "Content (HTML allowed)", type = "text", required = true, placeholder = "<p>Write text here...</p>")
    private String content;

    @Override
    public ComponentType getType() {
        return ComponentType.PARAGRAPH;
    }
}

