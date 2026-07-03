package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import id.adiputera.demo.cms.admin.dto.SearchCriteria;
import id.adiputera.demo.cms.admin.dto.SearchField;
import id.adiputera.demo.cms.admin.dto.SearchOperator;
import id.adiputera.demo.cms.annotation.CmsSearchable;
import id.adiputera.demo.cms.annotation.CmsSearchables;
import id.adiputera.demo.cms.entity.Article;
import id.adiputera.demo.cms.entity.Event;
import id.adiputera.demo.cms.entity.ItemModel;
import id.adiputera.demo.cms.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemSearchService {

    private final EntityManager entityManager;

    private static final Map<String, Class<?>> TYPE_TO_CLASS = new HashMap<>();

    static {
        TYPE_TO_CLASS.put("product", Product.class);
        TYPE_TO_CLASS.put("article", Article.class);
        TYPE_TO_CLASS.put("event", Event.class);
    }

    private List<SearchField> getSearchFields(Class<?> clazz) {
        List<SearchField> fields = new ArrayList<>();
        if (clazz.isAnnotationPresent(CmsSearchables.class)) {
            CmsSearchables searchables = clazz.getAnnotation(CmsSearchables.class);
            for (CmsSearchable searchable : searchables.value()) {
                fields.add(new SearchField(searchable.name(), searchable.displayName(), searchable.type()));
            }
        } else if (clazz.isAnnotationPresent(CmsSearchable.class)) {
            CmsSearchable searchable = clazz.getAnnotation(CmsSearchable.class);
            fields.add(new SearchField(searchable.name(), searchable.displayName(), searchable.type()));
        }
        return fields;
    }

    public ItemSearchMetadataDTO getSearchMetadata(String type) {
        Class<?> clazz = TYPE_TO_CLASS.get(type.toLowerCase());
        if (clazz == null) {
            return new ItemSearchMetadataDTO(List.of());
        }
        return new ItemSearchMetadataDTO(getSearchFields(clazz));
    }

    private Set<String> getAllowedFields(Class<?> clazz) {
        return getSearchFields(clazz).stream()
                .map(SearchField::getName)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public List<ItemSearchResultDTO> searchItems(String type, List<SearchCriteria> criteria) {
        Class<?> clazz = TYPE_TO_CLASS.get(type.toLowerCase());

        if (clazz == null) {
            return List.of();
        }

        // Handle pk specially if passed
        for (SearchCriteria c : criteria) {
            if ("pk".equals(c.getField())) {
                return searchByPk(clazz, c.getValue());
            }
        }

        return executeSearch(clazz, criteria);
    }

    private ItemSearchResultDTO mapToDTO(Object entity) {
        if (entity instanceof ItemModel itemModel) {
            return itemModel.toItemSearchResultDTO();
        }
        return null;
    }

    private List<ItemSearchResultDTO> searchByPk(Class<?> clazz, String pk) {
        try {
            Long id = Long.parseLong(pk);
            Object entity = entityManager.find(clazz, id);
            if (entity != null) {
                ItemSearchResultDTO dto = mapToDTO(entity);
                return dto != null ? List.of(dto) : List.of();
            }
        } catch (NumberFormatException e) {
            // Not an ID, maybe a code?
            if (clazz == Product.class) {
                // Try searching by code
                return executeSearch(Product.class, List.of(new SearchCriteria("code", SearchOperator.EQUALS, pk)));
            }
        }
        return List.of();
    }

    private <T> List<ItemSearchResultDTO> executeSearch(Class<T> clazz, List<SearchCriteria> criteria) {
        Set<String> allowedFields = getAllowedFields(clazz);
        StringBuilder jpql = new StringBuilder("SELECT x FROM ").append(clazz.getSimpleName()).append(" x WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        for (int i = 0; i < criteria.size(); i++) {
            SearchCriteria c = criteria.get(i);
            String key = c.getField();
            String value = c.getValue();
            if (allowedFields.contains(key) && value != null && !value.trim().isEmpty()) {
                String paramName = key + i;
                switch (c.getOperator() != null ? c.getOperator() : SearchOperator.CONTAINS) {
                    case EQUALS:
                        jpql.append(" AND x.").append(key).append(" = :").append(paramName);
                        params.put(paramName, value.trim());
                        break;
                    case GREATER_THAN:
                        jpql.append(" AND x.").append(key).append(" > :").append(paramName);
                        params.put(paramName, value.trim());
                        break;
                    case LESS_THAN:
                        jpql.append(" AND x.").append(key).append(" < :").append(paramName);
                        params.put(paramName, value.trim());
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
        return results.stream()
                .map(this::mapToDTO)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}
