package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

/**
 * Field handler implementation for STRING type.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class StringFieldHandler implements CmsFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The STRING type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.STRING;
    }

    /**
     * Converts a raw value to a String.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value.
     * @param meta The field metadata.
     * @return The converted String value, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Validates if the string value satisfies constraints.
     *
     * @see CmsFieldHandler#validate(Object, CmsFieldMetadata, boolean)
     * @param value The value to validate.
     * @param meta The field metadata.
     * @param isCreate True if this is a creation operation.
     */
    @Override
    public void validate(Object value, CmsFieldMetadata meta, boolean isCreate) {
        if (meta.isRequired() && (value == null || value.toString().trim().isEmpty())) {
            throw new BadRequestException("Field '" + meta.getDisplayName() + "' is required");
        }
    }

    /**
     * Serializes the string value using the field's formatter.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value.
     * @param meta The field metadata.
     * @return The formatted representation.
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        return meta.getFormatter().format(value);
    }
}
