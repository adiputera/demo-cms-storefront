package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

/**
 * Field handler implementation for ENUM type.
 * Handles Java enum fields by converting string values to enum constants.
 * Validation is performed by GenericEntityMapper via Enum.valueOf().
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class EnumFieldHandler implements CmsFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The ENUM type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.ENUM;
    }

    /**
     * Converts a raw value to a String (enum constant name).
     * Actual enum conversion happens in GenericEntityMapper.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value.
     * @param meta The field metadata.
     * @return The enum constant name as String, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * Validates if the enum value is required when necessary.
     * Actual enum constant validation is handled by GenericEntityMapper via Enum.valueOf().
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
     * Serializes the enum value.
     * If value is an Enum, returns its name. Otherwise returns formatted value.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value.
     * @param meta The field metadata.
     * @return The enum name or formatted representation.
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }
        return meta.getFormatter().format(value);
    }
}
