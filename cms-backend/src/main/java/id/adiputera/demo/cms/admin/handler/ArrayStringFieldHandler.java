package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Field handler implementation for ARRAY_STRING type.
 * Handles conversion between string arrays/lists and comma-separated strings.
 * Supports both String field type (comma-separated) and collection types.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class ArrayStringFieldHandler implements CmsFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The ARRAY_STRING type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.ARRAY_STRING;
    }

    /**
     * Converts a raw value to the appropriate collection or comma-separated string.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value (List or comma-separated string).
     * @param meta The field metadata.
     * @return The converted value (String or collection), or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }

        Class<?> targetType = meta.getGetter().getReturnType();
        
        // If input is already a list
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            
            // If target field is String, convert to comma-separated
            if (targetType == String.class) {
                return String.join(",", list);
            }
            // If target is a collection, return as-is
            return list;
        }

        // If input is a string
        String strVal = value.toString().trim();
        if (strVal.isEmpty()) {
            // Return empty string for String fields, empty list for collections
            return targetType == String.class ? "" : new ArrayList<>();
        }

        // If target field is String, return as-is
        if (targetType == String.class) {
            return strVal;
        }

        // If target is a collection, split comma-separated string
        String[] parts = strVal.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    /**
     * Validates if the array value satisfies constraints.
     *
     * @see CmsFieldHandler#validate(Object, CmsFieldMetadata, boolean)
     * @param value The value to validate.
     * @param meta The field metadata.
     * @param isCreate True if this is a creation operation.
     */
    @Override
    public void validate(Object value, CmsFieldMetadata meta, boolean isCreate) {
        if (!meta.isRequired()) {
            return;
        }

        // Check if required field has content
        if (value == null) {
            throw new BadRequestException("Field '" + meta.getDisplayName() + "' is required");
        }

        if (value instanceof List) {
            if (((List<?>) value).isEmpty()) {
                throw new BadRequestException("Field '" + meta.getDisplayName() + "' is required");
            }
        } else if (value instanceof String) {
            if (value.toString().trim().isEmpty()) {
                throw new BadRequestException("Field '" + meta.getDisplayName() + "' is required");
            }
        }
    }

    /**
     * Serializes the array value for frontend consumption.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value (String or collection).
     * @param meta The field metadata.
     * @return The formatted representation (List<String> for JSON).
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return new ArrayList<>();
        }

        // If already a list, return as-is
        if (value instanceof List) {
            return value;
        }

        // If it's a comma-separated string, convert to list for JSON
        String strVal = value.toString().trim();
        if (strVal.isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = strVal.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
