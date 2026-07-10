package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;

/**
 * Interface for type-specific CMS field handling.
 * Provides extensible hook points for converting, validating, and serializing CMS fields.
 *
 * @author Yusuf F. Adiputera
 */
public interface CmsFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @return The supported field type.
     */
    CmsFieldType getSupportedType();

    /**
     * Converts a raw value from frontend payload to entity field type.
     *
     * @param value The raw input value.
     * @param meta The field metadata.
     * @return The converted value.
     */
    Object convert(Object value, CmsFieldMetadata meta);

    /**
     * Validates a field value based on metadata and operation type.
     *
     * @param value The value to validate.
     * @param meta The field metadata.
     * @param isCreate True if this is a creation operation, false for updates.
     */
    void validate(Object value, CmsFieldMetadata meta, boolean isCreate);

    /**
     * Serializes an entity field value to return to the frontend.
     *
     * @param value The raw field value.
     * @param meta The field metadata.
     * @return The serialized value.
     */
    Object serialize(Object value, CmsFieldMetadata meta);
}
