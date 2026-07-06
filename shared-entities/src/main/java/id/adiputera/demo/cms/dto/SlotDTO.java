package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Slot D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {
    private Long id;
    private String code;
    private String name;
    private Long pageId;
    private List<ComponentDTO> components;
    
    private String syncStatus;
}
