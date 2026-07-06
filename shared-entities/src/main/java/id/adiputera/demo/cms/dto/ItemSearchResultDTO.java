package id.adiputera.demo.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item Search Result D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchResultDTO {
    private String id;
    private String label;
    private String subLabel;
}
