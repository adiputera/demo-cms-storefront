package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.exception.BadRequestException;
import id.adiputera.demo.cms.admin.handler.CmsFieldHandler;
import id.adiputera.demo.cms.admin.handler.CmsFieldHandlerRegistry;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Validates entity payloads against CMS field metadata and lifecycle rules.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
public class CmsValidator {

    private final CmsFieldHandlerRegistry handlerRegistry;

    /**
     * Validates a JSON input map payload against the schema metadata constraints.
     *
     * @param payload The request input payload.
     * @param typeMeta The entity schema metadata.
     * @param isCreate True if creating a new entity, false if updating.
     * @throws BadRequestException If any constraints (required or editable) are violated.
     */
    public void validate(Map<String, Object> payload, CmsTypeMetadata typeMeta, boolean isCreate) {
        for (CmsFieldMetadata fieldMeta : typeMeta.getFields()) {
            // For updates: only validate fields that are present in the payload.
            // This allows partial updates (only changed fields need to be sent).
            if (!isCreate && !payload.containsKey(fieldMeta.getName())) {
                continue;
            }

            Object val = payload.get(fieldMeta.getName());

            // 1. Validate editable constraint (reject non-editable fields sent on update)
            boolean isEditable = isCreate ? fieldMeta.isEditableOnCreate() : fieldMeta.isEditableOnUpdate();
            if (!isEditable && !isCreate && payload.containsKey(fieldMeta.getName())) {
                // Skip silently — populateEntity already filters non-editable fields
                continue;
            }

            // 2. Delegate type-specific validation (required check, format check) to the field handler
            CmsFieldHandler handler = handlerRegistry.getHandler(fieldMeta.getType());
            handler.validate(val, fieldMeta, isCreate);
        }
    }
}
