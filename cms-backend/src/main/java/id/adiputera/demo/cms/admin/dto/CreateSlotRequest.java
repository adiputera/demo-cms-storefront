package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Slot Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSlotRequest {
    
    @NotBlank(message = "Slot code is required")
    @Size(max = 100, message = "Code must be at most 100 characters")
    private String code;
    
    @NotBlank(message = "Slot name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;
    
    @NotNull(message = "Page ID is required")
    private Long pageId;
    
    private Integer sortOrder;
}
