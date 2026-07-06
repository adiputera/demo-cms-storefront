package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Create Banner Component Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateBannerComponentRequest extends CreateComponentRequest {
    private String imageUrl;
    private String altText;
    private String title;
    private String subtitle;
    private String ctaText;
    private String ctaUrl;
}
