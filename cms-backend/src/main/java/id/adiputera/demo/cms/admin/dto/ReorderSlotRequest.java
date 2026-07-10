package id.adiputera.demo.cms.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reorder Slot Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderSlotRequest {
    
    @NotNull(message = "New sort order is required")
    private Integer sortOrder;
}
