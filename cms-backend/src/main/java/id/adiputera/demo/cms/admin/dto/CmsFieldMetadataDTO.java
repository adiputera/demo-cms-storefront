package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for field metadata used by the frontend for forms and validation.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsFieldMetadataDTO {
    private String name;
    private String displayName;
    private String type;
    private boolean required;
    private boolean editableOnUpdate;
    private String placeholder;
    private String reference;
    private String referenceCardinality;
    private int order;
}
