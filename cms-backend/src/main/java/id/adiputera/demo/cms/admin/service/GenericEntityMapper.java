package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.CmsRowDTO;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import id.adiputera.demo.cms.admin.handler.CmsFieldHandler;
import id.adiputera.demo.cms.admin.handler.CmsFieldHandlerRegistry;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeMetadata;
import id.adiputera.demo.cms.entity.ItemModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic mapper service that transforms domain entities into standard tabular CmsRowDTOs
 * and populates entity properties from raw inbound payloads.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenericEntityMapper {

    private final CmsFieldHandlerRegistry handlerRegistry;

    /**
     * Maps a single ItemModel entity to its generic CmsRowDTO representation.
     *
     * @param entity The domain entity extending {@link ItemModel}.
     * @param typeMetadata The runtime metadata of the entity's type.
     * @return The populated {@link CmsRowDTO}.
     */
    public CmsRowDTO mapToRow(ItemModel entity, CmsTypeMetadata typeMetadata) {
        if (entity == null || typeMetadata == null) {
            return null;
        }

        String id = entity.getId() != null ? String.valueOf(entity.getId()) : null;
        Map<String, Object> values = new HashMap<>();

        for (CmsFieldMetadata fieldMeta : typeMetadata.getFields()) {
            Method getter = fieldMeta.getGetter();
            Object rawValue = null;
            if (getter != null) {
                try {
                    rawValue = getter.invoke(entity);
                } catch (Exception e) {
                    log.error("Failed to invoke getter method {} on entity: {}", getter.getName(), entity.getClass().getName(), e);
                }
            }
            CmsFieldHandler handler = handlerRegistry.getHandler(fieldMeta.getType());
            Object convertedValue = handler.serialize(rawValue, fieldMeta);
            values.put(fieldMeta.getName(), convertedValue);
        }

        // Include standard common properties if not explicitly mapped
        if (!values.containsKey("createdAt") && entity.getCreatedAt() != null) {
            values.put("createdAt", entity.getCreatedAt().toString());
        }
        if (!values.containsKey("updatedAt") && entity.getUpdatedAt() != null) {
            values.put("updatedAt", entity.getUpdatedAt().toString());
        }

        return CmsRowDTO.builder()
                .id(id)
                .values(values)
                .build();
    }

    /**
     * Maps a list of entities to a list of CmsRowDTOs.
     *
     * @param entities The list of entities to map.
     * @param typeMetadata The runtime metadata of the entities' type.
     * @return A list of {@link CmsRowDTO}s.
     */
    public List<CmsRowDTO> mapToRows(List<? extends ItemModel> entities, CmsTypeMetadata typeMetadata) {
        if (entities == null || typeMetadata == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> mapToRow(entity, typeMetadata))
                .collect(Collectors.toList());
    }

    /**
     * Populates entity properties from raw JSON payload using cached setters and converters.
     *
     * @param entity The target domain entity.
     * @param payload The request input payload.
     * @param typeMeta The schema metadata.
     * @param isCreate True if creating a new entity, false if updating.
     * @throws BadRequestException If setting property fails or type validation fails.
     */
    public void populateEntity(Object entity, Map<String, Object> payload, CmsTypeMetadata typeMeta, boolean isCreate) {
        for (CmsFieldMetadata fieldMeta : typeMeta.getFields()) {
            boolean isEditable = isCreate ? fieldMeta.isEditableOnCreate() : fieldMeta.isEditableOnUpdate();
            if (!isEditable) {
                continue;
            }

            if (!payload.containsKey(fieldMeta.getName())) {
                continue;
            }

            Object rawVal = payload.get(fieldMeta.getName());
            CmsFieldHandler handler = handlerRegistry.getHandler(fieldMeta.getType());
            Object convertedVal = handler.convert(rawVal, fieldMeta);

            Method setter = fieldMeta.getSetter();
            if (setter != null) {
                try {
                    // Coerce to enum if setter parameter is an enum type
                    Class<?> paramType = setter.getParameterTypes()[0];
                    if (paramType.isEnum() && convertedVal instanceof String) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Object enumVal = Enum.valueOf((Class<Enum>) paramType, ((String) convertedVal).toUpperCase());
                        setter.invoke(entity, enumVal);
                    } else {
                        setter.invoke(entity, convertedVal);
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Type mismatch invoking setter {} on {}: expected {}, got {}",
                            setter.getName(), entity.getClass().getName(),
                            setter.getParameterTypes()[0].getSimpleName(),
                            convertedVal != null ? convertedVal.getClass().getSimpleName() : "null");
                    throw new BadRequestException("Invalid value for field '" + fieldMeta.getDisplayName() + "': " + e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Failed to invoke setter method {} on entity: {}", setter.getName(), entity.getClass().getName(), e);
                    throw new BadRequestException("Failed to set field '" + fieldMeta.getDisplayName() + "'", e);
                }
            }
        }
    }
}

