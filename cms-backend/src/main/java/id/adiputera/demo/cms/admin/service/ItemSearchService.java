package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.ItemSearchMetadataDTO;
import id.adiputera.demo.cms.admin.dto.ItemSearchResultDTO;
import id.adiputera.demo.cms.admin.dto.SearchField;
import id.adiputera.demo.cms.annotation.CmsSearchable;
import id.adiputera.demo.cms.annotation.CmsSearchables;
import id.adiputera.demo.cms.entity.Article;
import id.adiputera.demo.cms.entity.Event;
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

    public ItemSearchMetadataDTO getSearchMetadata(String type) {
        Class<?> clazz = TYPE_TO_CLASS.get(type.toLowerCase());
        if (clazz == null) {
            return new ItemSearchMetadataDTO(List.of());
        }

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

        return new ItemSearchMetadataDTO(fields);
    }

    @SuppressWarnings("unchecked")
    public List<ItemSearchResultDTO> searchItems(String type, Map<String, String> criteria) {
        Class<?> clazz = TYPE_TO_CLASS.get(type.toLowerCase());

        if (clazz == null) {
            return List.of();
        }

        if (criteria.containsKey("pk")) {
            return searchByPk(clazz, criteria.get("pk"));
        }

        if (type.equalsIgnoreCase("product")) {
            return searchProducts(criteria);
        } else if (type.equalsIgnoreCase("article")) {
            return searchArticles(criteria);
        } else if (type.equalsIgnoreCase("event")) {
            return searchEvents(criteria);
        }

        return List.of();
    }

    private List<ItemSearchResultDTO> searchByPk(Class<?> clazz, String pk) {
        try {
            Long id = Long.parseLong(pk);
            Object entity = entityManager.find(clazz, id);
            if (entity != null) {
                if (entity instanceof Product p) {
                    return List.of(new ItemSearchResultDTO(p.getCode(), p.getName(), p.getCode() + " - $" + p.getPrice()));
                } else if (entity instanceof Article a) {
                    return List.of(new ItemSearchResultDTO(String.valueOf(a.getId()), a.getTitle(), "Article"));
                } else if (entity instanceof Event e) {
                    return List.of(new ItemSearchResultDTO(String.valueOf(e.getId()), e.getTitle(), e.getLocation()));
                }
            }
        } catch (NumberFormatException e) {
            // Not an ID, maybe a code?
            if (clazz == Product.class) {
                // Try searching by code
                Map<String, String> c = new HashMap<>();
                c.put("code", pk);
                return searchProducts(c);
            }
        }
        return List.of();
    }

    private List<ItemSearchResultDTO> searchProducts(Map<String, String> criteria) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (criteria.containsKey("name") && !criteria.get("name").trim().isEmpty()) {
            jpql.append(" AND LOWER(p.name) LIKE :name");
            params.put("name", "%" + criteria.get("name").trim().toLowerCase() + "%");
        }
        if (criteria.containsKey("code") && !criteria.get("code").trim().isEmpty()) {
            jpql.append(" AND LOWER(p.code) LIKE :code");
            params.put("code", "%" + criteria.get("code").trim().toLowerCase() + "%");
        }

        Query query = entityManager.createQuery(jpql.toString(), Product.class);
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        List<Product> products = query.getResultList();
        return products.stream()
                .map(p -> new ItemSearchResultDTO(p.getCode(), p.getName(), p.getCode() + " - $" + p.getPrice()))
                .collect(Collectors.toList());
    }

    private List<ItemSearchResultDTO> searchArticles(Map<String, String> criteria) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM Article a WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (criteria.containsKey("title") && !criteria.get("title").trim().isEmpty()) {
            jpql.append(" AND LOWER(a.title) LIKE :title");
            params.put("title", "%" + criteria.get("title").trim().toLowerCase() + "%");
        }

        Query query = entityManager.createQuery(jpql.toString(), Article.class);
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        List<Article> articles = query.getResultList();
        return articles.stream()
                .map(a -> new ItemSearchResultDTO(String.valueOf(a.getId()), a.getTitle(), "Article"))
                .collect(Collectors.toList());
    }

    private List<ItemSearchResultDTO> searchEvents(Map<String, String> criteria) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM Event e WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (criteria.containsKey("title") && !criteria.get("title").trim().isEmpty()) {
            jpql.append(" AND LOWER(e.title) LIKE :title");
            params.put("title", "%" + criteria.get("title").trim().toLowerCase() + "%");
        }
        if (criteria.containsKey("location") && !criteria.get("location").trim().isEmpty()) {
            jpql.append(" AND LOWER(e.location) LIKE :location");
            params.put("location", "%" + criteria.get("location").trim().toLowerCase() + "%");
        }

        Query query = entityManager.createQuery(jpql.toString(), Event.class);
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        List<Event> events = query.getResultList();
        return events.stream()
                .map(e -> new ItemSearchResultDTO(String.valueOf(e.getId()), e.getTitle(), e.getLocation()))
                .collect(Collectors.toList());
    }
}
