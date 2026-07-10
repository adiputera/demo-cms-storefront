package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standard DTO representing a single tabular data row for generic entities.
 * Decouples entity-specific models from the dynamic frontend data tables.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsRowDTO {

    private String id;
    private Map<String, Object> values;
}
