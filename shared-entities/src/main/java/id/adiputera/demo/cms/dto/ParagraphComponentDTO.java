package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Paragraph Component D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParagraphComponentDTO extends ComponentDTO {
    private String title;
    private String content;
}
