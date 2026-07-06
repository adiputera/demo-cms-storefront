package id.adiputera.demo.cms.storefront.dto;

import id.adiputera.demo.cms.dto.SlotDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Slot Details Response class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDetailsResponse {
    private List<SlotDTO> slots;
}
