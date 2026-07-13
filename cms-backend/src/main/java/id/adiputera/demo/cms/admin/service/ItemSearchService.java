package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.CmsFieldMetadataDTO;
import id.adiputera.demo.cms.admin.dto.CmsRowDTO;
import id.adiputera.demo.cms.admin.dto.ItemMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ModelInfoDTO;
import id.adiputera.demo.cms.admin.dto.SearchCriteria;
import id.adiputera.demo.cms.admin.dto.SearchField;
import id.adiputera.demo.cms.admin.dto.SearchOperator;
import id.adiputera.demo.cms.admin.metadata.CmsFieldMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeRegistry;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import id.adiputera.demo.cms.entity.ItemModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Item Search Service class providing dynamic search capabilities.
 * Manages generic queries, metadata discovery, and row mappings.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemSearchService {

    private final EntityManager entityManager;
    private final CmsTypeRegistry cmsTypeRegistry;
    private final GenericEntityMapper genericEntityMapper;

    /**
     * Retrieves all registered available model types.
     *
     * @return A list of {@link ModelInfoDTO} representing available models.
     */
    public List<ModelInfoDTO> getAvailableTypes() {
        return cmsTypeRegistry.getAllTypes().stream()
                .map(meta -> ModelInfoDTO.builder()
                        .type(meta.getCode())
                        .displayName(meta.getDisplayName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves unified metadata (searchable fields and display columns) for a given type.
     *
     * @param type The lower-case model type code.
     * @return The {@link ItemMetadataDTO} containing dynamic lists of fields, or null if type not registered.
     */
    public ItemMetadataDTO getUnifiedMetadata(String type) {
        CmsTypeMetadata meta = cmsTypeRegistry.getTypeMetadata(type);
        if (meta == null) {
            return null;
        }

        List<SearchField> searchable = meta.getFields().stream()
                .filter(CmsFieldMetadata::isSearchable)
                .map(f -> new SearchField(f.getName(), f.getDisplayName(), f.getType().name().toLowerCase(), f.getOrder()))
                .collect(Collectors.toList());

        List<SearchField> columnShown = meta.getFields().stream()
                .filter(CmsFieldMetadata::isShowAsColumn)
                .map(f -> new SearchField(f.getName(), f.getDisplayName(), f.getType().name().toLowerCase(), f.getOrder()))
                .collect(Collectors.toList());

        List<CmsFieldMetadataDTO> fields = meta.getFields().stream()
                .map(f -> CmsFieldMetadataDTO.builder()
                        .name(f.getName())
                        .displayName(f.getDisplayName())
                        .type(f.getType().name())
                        .required(f.isRequired())
                        .editableOnUpdate(f.isEditableOnUpdate())
                        .placeholder(f.getPlaceholder())
                        .reference(f.getTargetEntity() != null && f.getTargetEntity() != id.adiputera.demo.cms.entity.ItemModel.class ? f.getTargetEntity().getName() : null)
                        .referenceCardinality(f.getCardinality() != null ? f.getCardinality().name() : null)
                        .order(f.getOrder())
                        .enumValues(f.getEnumConstants())
                        .build())
                .collect(Collectors.toList());

        return ItemMetadataDTO.builder()
                .code(meta.getCode())
                .displayName(meta.getDisplayName())
                .searchable(searchable)
                .columnShown(columnShown)
                .fields(fields)
                .build();
    }

    /**
     * Gets the search metadata for a given item type.
     *
     * @param type The item type string.
     * @return The search metadata DTO.
     */
    public ItemSearchMetadataDTO getSearchMetadata(String type) {
        CmsTypeMetadata meta = cmsTypeRegistry.getTypeMetadata(type);
        if (meta == null) {
            return new ItemSearchMetadataDTO(List.of());
        }
        List<SearchField> fields = meta.getFields().stream()
                .filter(CmsFieldMetadata::isSearchable)
                .map(f -> new SearchField(f.getName(), f.getDisplayName(), f.getType().name().toLowerCase(), f.getOrder()))
                .collect(Collectors.toList());
        return new ItemSearchMetadataDTO(fields);
    }

    /**
     * Gets the set of allowed searchable field names for a given class.
     * Includes the standard database entity identifier "id" to permit programmatic
     * lookups.
     *
     * @param meta The item type metadata.
     * @return The set of allowed field names.
     */
    private Set<String> getAllowedFields(CmsTypeMetadata meta) {
        Set<String> allowed = meta.getFields().stream()
                .filter(CmsFieldMetadata::isSearchable)
                .map(CmsFieldMetadata::getName)
                .collect(Collectors.toSet());
        allowed.add("id");
        return allowed;
    }

    /**
     * Searches items based on type and criteria.
     *
     * @param type     The item type string.
     * @param criteria The list of search criteria.
     * @return A list of matching item search result DTOs.
     */
    public List<ItemSearchResultDTO> searchItems(String type, List<SearchCriteria> criteria) {
        CmsTypeMetadata meta = cmsTypeRegistry.getTypeMetadata(type);
        if (meta == null) {
            return List.of();
        }
        List<?> results = executeQuery(meta, criteria);
        return results.stream()
                .map(this::mapToDTO)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Generic dynamic search executing criteria query and returning polymorphic row DTOs.
     *
     * @param type     The model type string (e.g. "product").
     * @param criteria The search criteria.
     * @return A list of generic {@link CmsRowDTO}s.
     */
    @SuppressWarnings("unchecked")
    public List<CmsRowDTO> searchItemsForList(String type, List<SearchCriteria> criteria) {
        CmsTypeMetadata meta = cmsTypeRegistry.getTypeMetadata(type);
        if (meta == null) {
            return List.of();
        }
        List<?> entities = executeQuery(meta, criteria);
        return genericEntityMapper.mapToRows((List<? extends ItemModel>) entities, meta);
    }

    /**
     * Maps an entity object to an ItemSearchResultDTO.
     *
     * @param entity The entity object.
     * @return The mapped DTO or null if not an ItemModel.
     */
    private ItemSearchResultDTO mapToDTO(Object entity) {
        if (entity instanceof ItemModel itemModel) {
            return itemModel.toItemSearchResultDTO();
        }
        return null;
    }

    /**
     * Checks if a field is of numeric type.
     *
     * @param fieldMeta The field metadata.
     * @param fieldName The field name.
     * @return True if the field is numeric, false otherwise.
     */
    private boolean isNumericField(CmsFieldMetadata fieldMeta, String fieldName) {
        if ("id".equals(fieldName)) {
            return true;
        }
        return fieldMeta != null && fieldMeta.getType() == id.adiputera.demo.cms.annotation.CmsFieldType.NUMBER;
    }

    /**
     * Converts a string parameter value to the appropriate Java type for the field.
     *
     * @param fieldMeta   The field metadata.
     * @param fieldName   The field name.
     * @param valueString The string value.
     * @return The converted object or original string if conversion fails or is not
     *         needed.
     */
    private Object convertParamValue(CmsFieldMetadata fieldMeta, String fieldName, String valueString) {
        if (valueString == null) {
            return null;
        }
        String trimmed = valueString.trim();
        if ("id".equals(fieldName)) {
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                return trimmed;
            }
        }
        if (fieldMeta == null || fieldMeta.getGetter() == null) {
            return trimmed;
        }
        try {
            Class<?> type = fieldMeta.getGetter().getReturnType();
            if (type == java.math.BigDecimal.class) {
                return new java.math.BigDecimal(trimmed);
            } else if (type == Integer.class || type == int.class) {
                return Integer.parseInt(trimmed);
            } else if (type == Long.class || type == long.class) {
                return Long.parseLong(trimmed);
            } else if (type == Double.class || type == double.class) {
                return Double.parseDouble(trimmed);
            } else if (type == Float.class || type == float.class) {
                return Float.parseFloat(trimmed);
            } else if (type == Short.class || type == short.class) {
                return Short.parseShort(trimmed);
            } else if (type == Boolean.class || type == boolean.class) {
                return Boolean.parseBoolean(trimmed);
            }
            return trimmed;
        } catch (Exception e) {
            log.warn("Could not convert value '{}' to field type for field {}: {}", trimmed, fieldName, e.getMessage());
        }
        return trimmed;
    }

    /**
     * Executes the JPQL search query and returns the matching entity entities.
     *
     * @param meta     The item type metadata.
     * @param criteria The list of search criteria.
     * @param <T>      The entity type.
     * @return A list of entity objects.
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> executeQuery(CmsTypeMetadata meta, List<SearchCriteria> criteria) {
        Class<T> clazz = (Class<T>) meta.getEntityClass();
        Set<String> allowedFields = getAllowedFields(meta);
        StringBuilder jpql = new StringBuilder("SELECT x FROM ").append(clazz.getSimpleName()).append(" x WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        for (int i = 0; i < criteria.size(); i++) {
            SearchCriteria c = criteria.get(i);
            String key = c.getField();
            String value = c.getValue();
            if (allowedFields.contains(key) && value != null && !value.trim().isEmpty()) {
                String paramName = key + i;
                SearchOperator operator = c.getOperator() != null ? c.getOperator() : SearchOperator.CONTAINS;
                
                final String fieldKey = key;
                CmsFieldMetadata fieldMeta = meta.getFields().stream()
                        .filter(f -> f.getName().equals(fieldKey))
                        .findFirst()
                        .orElse(null);
                
                boolean numeric = isNumericField(fieldMeta, key);
                if (numeric && operator == SearchOperator.CONTAINS) {
                    operator = SearchOperator.EQUALS;
                }

                Object paramValue = convertParamValue(fieldMeta, key, value);

                switch (operator) {
                    case EQUALS:
                        jpql.append(" AND x.").append(key).append(" = :").append(paramName);
                        params.put(paramName, paramValue);
                        break;
                    case MORE_THAN:
                        jpql.append(" AND x.").append(key).append(" > :").append(paramName);
                        params.put(paramName, paramValue);
                        break;
                    case LESS_THAN:
                        jpql.append(" AND x.").append(key).append(" < :").append(paramName);
                        params.put(paramName, paramValue);
                        break;
                    case CONTAINS:
                    default:
                        jpql.append(" AND x.").append(key).append(" LIKE :").append(paramName);
                        params.put(paramName, "%" + value.trim() + "%");
                        break;
                }
            }
        }

        Query query = entityManager.createQuery(jpql.toString(), clazz);
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        @SuppressWarnings("unchecked")
        List<T> results = query.getResultList();
        return results;
    }
}
