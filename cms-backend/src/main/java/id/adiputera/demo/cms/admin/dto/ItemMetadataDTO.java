package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing unified metadata (searchable and columnShown fields) for an entity type.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemMetadataDTO {
    private String code;
    private String displayName;
    private List<SearchField> searchable;
    private List<SearchField> columnShown;
    private List<CmsFieldMetadataDTO> fields;
}
