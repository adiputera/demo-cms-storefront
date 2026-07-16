package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.annotation.ReferenceCardinality;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeRegistry;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import id.adiputera.demo.cms.entity.ItemModel;
import id.adiputera.demo.cms.entity.CatalogAwareModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Field handler implementation for REFERENCE type.
 * Handles both real JPA entity relationships and collections stored as comma-separated strings.
 *
 * @author Yusuf F. Adiputera
 */
@Component
@RequiredArgsConstructor
public class ReferenceFieldHandler implements CmsFieldHandler {

    private final EntityManager entityManager;
    private final CmsTypeRegistry cmsTypeRegistry;

    /**
     * Gets the supported CmsFieldType.
     *
     * @see CmsFieldHandler#getSupportedType()
     * @return The REFERENCE type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.REFERENCE;
    }

    /**
     * Converts raw input to entity reference field type.
     *
     * @see CmsFieldHandler#convert(Object, CmsFieldMetadata)
     * @param value The raw input value.
     * @param meta The field metadata.
     * @return The entity reference proxy, comma-separated string, or null.
     */
    @Override
    public Object convert(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        Class<?> fieldType = meta.getGetter().getReturnType();

        if (ItemModel.class.isAssignableFrom(fieldType)) {
            Object idVal = value;
            if (value instanceof Map) {
                idVal = ((Map<?, ?>) value).get("id");
            }
            if (idVal == null || idVal.toString().trim().isEmpty()) {
                return null;
            }
            try {
                Long id = Long.valueOf(idVal.toString());
                return entityManager.getReference(fieldType, id);
            } catch (Exception e) {
                throw new BadRequestException("Invalid reference ID: " + idVal + " for field " + meta.getDisplayName(), e);
            }
        }

        if (fieldType == String.class) {
            if (value instanceof Collection) {
                Collection<?> col = (Collection<?>) value;
                List<String> parts = new ArrayList<>();
                for (Object item : col) {
                    if (item == null) {
                        continue;
                    }
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        Object val = map.get("id");
                        if (val != null) {
                            parts.add(val.toString());
                        }
                    } else {
                        parts.add(item.toString());
                    }
                }
                return String.join(",", parts);
            }
            return value.toString();
        }

        return value;
    }

    /**
     * Validates that reference values satisfy constraints.
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
     * Serializes reference values. Returns map metadata for entities, or raw values.
     *
     * @see CmsFieldHandler#serialize(Object, CmsFieldMetadata)
     * @param value The raw field value.
     * @param meta The field metadata.
     * @return Map structure for JPA entities or raw formatted value.
     */
    @Override
    public Object serialize(Object value, CmsFieldMetadata meta) {
        if (value == null) {
            return null;
        }
        
        // Handle MULTIPLE String references (e.g. "mbp-16,iphone-16")
        if (meta.getCardinality() == ReferenceCardinality.MULTIPLE) {
            if (value instanceof String str) {
                String[] parts = str.split(",");
                List<Map<String, Object>> result = new ArrayList<>();
                Class<?> refClass = meta.getTargetEntity();
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    String label = getDisplayNameFromSyncKey(trimmed, refClass);
                    result.add(Map.of(
                        "id", trimmed,
                        "displayName", label
                    ));
                }
                return result;
            }
        }
        
        // Handle SINGLE String references (e.g. "mbp-16")
        if (meta.getTargetEntity() != null && value instanceof String str) {
            String label = getDisplayNameFromSyncKey(str, meta.getTargetEntity());
            return Map.of(
                "id", str,
                "displayName", label
            );
        }
        
        // Handle actual @ManyToOne Entity references
        if (value instanceof ItemModel item) {
            String label = item.toItemSearchResultDTO().getLabel();
            return Map.of(
                "id", item.getId(),
                "displayName", label
            );
        }
        return meta.getFormatter().format(value);
    }

    /**
     * Resolves the display name label for a referenced entity given its sync key.
     *
     * @param syncKey The sync key identifier.
     * @param refClass The target class of the reference.
     * @return The formatted label string, or the sync key itself if lookup fails.
     */
    private String getDisplayNameFromSyncKey(String syncKey, Class<?> refClass) {
        if (!CatalogAwareModel.class.isAssignableFrom(refClass)) {
            return syncKey;
        }
        try {
            CmsTypeMetadata typeMeta = cmsTypeRegistry.getTypeMetadata(refClass.getSimpleName().toLowerCase());
            String fieldName = typeMeta != null ? typeMeta.getSyncKeyFieldName() : null;
            if (fieldName == null) {
                return syncKey;
            }

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<?> cq = cb.createQuery(refClass);
            Root<?> root = cq.from(refClass);
            cq.where(cb.equal(root.get(fieldName), syncKey));
            
            List<?> results = entityManager.createQuery(cq).setMaxResults(1).getResultList();
            if (!results.isEmpty()) {
                CatalogAwareModel entity = (CatalogAwareModel) results.get(0);
                return entity.toItemSearchResultDTO().getLabel();
            }
        } catch (Exception e) {
            // ignore and fallback to syncKey
        }
        return syncKey;
    }
}
