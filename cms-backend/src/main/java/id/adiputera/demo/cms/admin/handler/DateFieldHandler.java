package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Field handler implementation for DATE type.
 * Handles conversion between ISO date strings (YYYY-MM-DD) and java.time.LocalDate.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class DateFieldHandler implements CmsFieldHandler {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The DATE type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.DATE;
    }

    /**
     * Converts a raw value to a LocalDate.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value (expected ISO date string YYYY-MM-DD).
     * @param meta The field metadata.
     * @return The converted LocalDate value, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate) {
            return value;
        }

        String strVal = value.toString().trim();
        if (strVal.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(strVal, ISO_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Field '" + meta.getDisplayName() + 
                "' must be a valid date in ISO format (YYYY-MM-DD)", e);
        }
    }

    /**
     * Validates if the date value satisfies constraints.
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
     * Serializes the date value using the field's formatter.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value (LocalDate).
     * @param meta The field metadata.
     * @return The formatted representation (ISO date string).
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(ISO_DATE_FORMATTER);
        }
        return meta.getFormatter().format(value);
    }
}
