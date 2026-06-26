package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateNavigationComponentRequest extends CreateComponentRequest {
    @NotBlank(message = "Display text is required")
    private String displayText;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    private String icon;
}
