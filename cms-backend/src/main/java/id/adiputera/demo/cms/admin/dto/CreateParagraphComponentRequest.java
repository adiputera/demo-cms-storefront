package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Create Paragraph Component Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateParagraphComponentRequest extends CreateComponentRequest {
    private String title;
    private String content;
}
