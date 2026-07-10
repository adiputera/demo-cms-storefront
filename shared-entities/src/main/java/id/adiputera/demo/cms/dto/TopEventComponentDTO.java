package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Top Event Component D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TopEventComponentDTO extends ComponentDTO {
    private String title;
    private String eventSlug;
}
