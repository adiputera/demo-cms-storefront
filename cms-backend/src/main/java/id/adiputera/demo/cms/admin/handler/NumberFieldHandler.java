package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Field handler implementation for NUMBER type.
 * Handles coercion from string or JSON numeric representation to Java numeric type.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class NumberFieldHandler implements CmsFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The NUMBER type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.NUMBER;
    }

    /**
     * Converts a raw value to the numeric type expected by the entity field.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value.
     * @param meta The field metadata.
     * @return The converted number, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }

        // Try extracting numeric value from structured JSON format (e.g., from currency formatters)
        Object rawVal = value;
        if (value instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) value;
            if (map.containsKey("value")) {
                rawVal = map.get("value");
            }
        }

        if (rawVal == null || rawVal.toString().trim().isEmpty()) {
            return null;
        }

        Class<?> targetType = meta.getGetter().getReturnType();
        String strVal = rawVal.toString();

        try {
            if (targetType == BigDecimal.class) {
                return new BigDecimal(strVal);
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(strVal);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(strVal);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(strVal);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(strVal);
            } else if (targetType == Short.class || targetType == short.class) {
                return Short.valueOf(strVal);
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Field '" + meta.getDisplayName() + "' must be a valid number", e);
        }

        return rawVal;
    }

    /**
     * Validates if the number satisfies required constraints.
     *
     * @see CmsFieldHandler#validate(Object, CmsFieldMetadata, boolean)
     * @param value The value to validate.
     * @param meta The field metadata.
     * @param isCreate True if this is a creation operation.
     */
    @Override
    public void validate(Object value, CmsFieldMetadata meta, boolean isCreate) {
        if (meta.isRequired() && value == null) {
            throw new BadRequestException("Field '" + meta.getDisplayName() + "' is required");
        }
    }

    /**
     * Serializes the numeric value using the field's formatter.
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
