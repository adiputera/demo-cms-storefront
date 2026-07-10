package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Field handler implementation for DATETIME type.
 * Handles conversion between ISO datetime strings and java.time.LocalDateTime.
 * Accepts formats: YYYY-MM-DDTHH:mm, YYYY-MM-DDTHH:mm:ss
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class DateTimeFieldHandler implements CmsFieldHandler {

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The DATETIME type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.DATETIME;
    }

    /**
     * Converts a raw value to a LocalDateTime.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value (expected ISO datetime string).
     * @param meta The field metadata.
     * @return The converted LocalDateTime value, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime) {
            return value;
        }

        String strVal = value.toString().trim();
        if (strVal.isEmpty()) {
            return null;
        }

        try {
            // Try parsing with ISO_LOCAL_DATE_TIME (handles both with and without seconds)
            return LocalDateTime.parse(strVal, ISO_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try alternative format without seconds if needed
            try {
                // Append :00 seconds if format is YYYY-MM-DDTHH:mm
                if (strVal.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
                    return LocalDateTime.parse(strVal + ":00", ISO_DATETIME_FORMATTER);
                }
            } catch (DateTimeParseException ignored) {
            }
            throw new BadRequestException("Field '" + meta.getDisplayName() + 
                "' must be a valid datetime in ISO format (YYYY-MM-DDTHH:mm:ss or YYYY-MM-DDTHH:mm)", e);
        }
    }

    /**
     * Validates if the datetime value satisfies constraints.
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
     * Serializes the datetime value using the field's formatter.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value (LocalDateTime).
     * @param meta The field metadata.
     * @return The formatted representation (ISO datetime string).
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(ISO_DATETIME_FORMATTER);
        }
        return meta.getFormatter().format(value);
    }
}
