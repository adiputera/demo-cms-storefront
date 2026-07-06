package id.adiputera.demo.cms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Navigation Component D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NavigationComponentDTO extends ComponentDTO {
    @NotBlank(message = "Display text is required")
    private String displayText;

    @NotBlank(message = "URL is required")
    private String url;

    private String icon;
}
