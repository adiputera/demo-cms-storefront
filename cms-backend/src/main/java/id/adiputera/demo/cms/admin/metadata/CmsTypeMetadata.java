package id.adiputera.demo.cms.admin.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Runtime metadata definition for an registered entity type.
 * Groups fields and core entity properties.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTypeMetadata {

    private String code;
    private String displayName;
    private Class<?> entityClass;
    private List<CmsFieldMetadata> fields;
}
