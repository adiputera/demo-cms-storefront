package id.adiputera.demo.cms.admin.dto;

import id.adiputera.demo.cms.dto.ComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Slot Response class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {
    
    private Long id;
    private String code;
    private String name;
    private Long pageId;
    
    @Builder.Default
    private List<ComponentDTO> components = new ArrayList<>();

    private String syncStatus;
}
