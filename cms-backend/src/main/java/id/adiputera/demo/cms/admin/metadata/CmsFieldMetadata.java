package id.adiputera.demo.cms.admin.metadata;

import java.lang.reflect.Method;
import java.util.List;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.converter.CmsFormatter;
import id.adiputera.demo.cms.converter.CmsValueConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runtime metadata definition for a single annotated CMS field.
 * Includes cached property getters and setters, value converters, formatters, and constraints.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsFieldMetadata {

    private String name;
    private String displayName;
    private CmsFieldType type;
    private int order;
    private boolean searchable;
    private boolean showAsColumn;
    private boolean required;
    private boolean editableOnUpdate;
    private String placeholder;
    private Class<? extends id.adiputera.demo.cms.entity.ItemModel> targetEntity;
    private id.adiputera.demo.cms.annotation.ReferenceCardinality cardinality;
    private CmsValueConverter valueConverter;
    private CmsFormatter formatter;
    private Method getter;
    private Method setter;
    private List<String> enumConstants;

    /**
     * Determines if the field is editable during creation.
     * A field is editable on create if it's required OR editable on update.
     *
     * @return True if editable on create.
     */
    public boolean isEditableOnCreate() {
        return required || editableOnUpdate;
    }
}
