package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breadcrumb D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadcrumbDTO {
    private String slug;
    private String breadcrumbTitle;
}
