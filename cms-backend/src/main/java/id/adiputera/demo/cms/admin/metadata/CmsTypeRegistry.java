package id.adiputera.demo.cms.admin.metadata;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.converter.CmsFormatter;
import id.adiputera.demo.cms.converter.CmsValueConverter;
import id.adiputera.demo.cms.converter.DefaultFormatter;
import id.adiputera.demo.cms.converter.DefaultValueConverter;
import id.adiputera.demo.cms.entity.ItemModel;
import id.adiputera.demo.cms.entity.CatalogAwareModel;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry service that scans, builds, and caches runtime metadata for CMS entities.
 * Scans all JPA entities extending {@link ItemModel} at startup.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CmsTypeRegistry {

    private final EntityManager entityManager;

    private final Map<String, CmsTypeMetadata> registry = new ConcurrentHashMap<>();
    private final Map<Class<? extends CmsValueConverter>, CmsValueConverter> converterCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends CmsFormatter>, CmsFormatter> formatterCache = new ConcurrentHashMap<>();

    /**
     * Scans the JPA metamodel and initializes the metadata cache at startup.
     */
    @PostConstruct
    public void init() {
        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            if (javaType != null && ItemModel.class.isAssignableFrom(javaType)) {
                registerType(javaType);
            }
        }
        log.info("CmsTypeRegistry initialized with registered type codes: {}", registry.keySet());
    }

    /**
     * Registers a single CMS entity type.
     *
     * @param clazz The entity class.
     */
    private void registerType(Class<?> clazz) {
        String typeCode = clazz.getSimpleName().toLowerCase();
        String displayName = clazz.getSimpleName();

        List<CmsFieldMetadata> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CmsField.class)) {
                    CmsField ann = field.getAnnotation(CmsField.class);
                    Method getter = findGetter(clazz, field.getName(), field.getType());
                    Method setter = findSetter(clazz, field.getName(), field.getType());

                    if (getter == null) {
                        log.warn("No getter found for field: {} in class: {}", field.getName(), clazz.getName());
                    }
                    if (setter == null) {
                        log.warn("No setter found for field: {} in class: {}", field.getName(), clazz.getName());
                    }

                    CmsValueConverter converter = getOrCreateConverter(ann.converter());
                    CmsFormatter formatter = getOrCreateFormatter(ann.formatter());

                    // Detect enum fields and extract constants
                    List<String> enumConstants = null;
                    if (ann.type() == CmsFieldType.ENUM && field.getType().isEnum()) {
                        enumConstants = new ArrayList<>();
                        Object[] constants = field.getType().getEnumConstants();
                        for (Object constant : constants) {
                            enumConstants.add(((Enum<?>) constant).name());
                        }
                    }

                    CmsFieldMetadata fieldMeta = CmsFieldMetadata.builder()
                            .name(field.getName())
                            .displayName(ann.displayName())
                            .type(ann.type())
                            .order(ann.order())
                            .searchable(ann.searchable())
                            .showAsColumn(ann.showAsColumn())
                            .required(ann.required())
                            .editableOnUpdate(ann.editableOnUpdate())
                            .placeholder(ann.placeholder())
                            .targetEntity(ann.targetEntity())
                            .cardinality(ann.cardinality())
                            .valueConverter(converter)
                            .formatter(formatter)
                            .getter(getter)
                            .setter(setter)
                            .enumConstants(enumConstants)
                            .build();
                    fields.add(fieldMeta);
                }
            }
            current = current.getSuperclass();
        }

        fields.sort(Comparator.comparingInt(CmsFieldMetadata::getOrder));

        String syncKeyFieldName = null;
        if (CatalogAwareModel.class.isAssignableFrom(clazz)) {
            try {
                CatalogAwareModel dummy = (CatalogAwareModel) clazz.getDeclaredConstructor().newInstance();
                syncKeyFieldName = dummy.getSyncKeyFieldName();
            } catch (Exception e) {
                log.warn("Failed to resolve syncKeyFieldName for class {}: {}", clazz.getName(), e.getMessage());
            }
        }

        CmsTypeMetadata typeMeta = CmsTypeMetadata.builder()
                .code(typeCode)
                .displayName(displayName)
                .entityClass(clazz)
                .fields(fields)
                .syncKeyFieldName(syncKeyFieldName)
                .build();

        registry.put(typeCode, typeMeta);
    }

    /**
     * Resolves the getter method for a given field name and class.
     *
     * @param clazz The entity class.
     * @param fieldName The name of the field.
     * @param fieldType The Java class type of the field.
     * @return The reflected getter {@link Method}, or null if not found.
     */
    private Method findGetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String prefix = (fieldType == boolean.class || fieldType == Boolean.class) ? "is" : "get";
        String getterName = prefix + capitalized;
        try {
            return clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            if (prefix.equals("is")) {
                try {
                    return clazz.getMethod("get" + capitalized);
                } catch (NoSuchMethodException ex) {
                    // fall through
                }
            }
        }
        return null;
    }

    /**
     * Resolves the setter method for a given field name and class.
     *
     * @param clazz The entity class.
     * @param fieldName The name of the field.
     * @param fieldType The Java class type of the field.
     * @return The reflected setter {@link Method}, or null if not found.
     */
    private Method findSetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String setterName = "set" + capitalized;
        try {
            return clazz.getMethod(setterName, fieldType);
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Looks up or instantiates a converter class.
     *
     * @param converterClass The class of the converter.
     * @return The instantiated {@link CmsValueConverter}.
     */
    private CmsValueConverter getOrCreateConverter(Class<? extends CmsValueConverter> converterClass) {
        return converterCache.computeIfAbsent(converterClass, clz -> {
            try {
                return clz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Failed to instantiate CmsValueConverter class: {}", clz.getName(), e);
                return new DefaultValueConverter();
            }
        });
    }

    /**
     * Looks up or instantiates a formatter class.
     *
     * @param formatterClass The class of the formatter.
     * @return The instantiated {@link CmsFormatter}.
     */
    private CmsFormatter getOrCreateFormatter(Class<? extends CmsFormatter> formatterClass) {
        return formatterCache.computeIfAbsent(formatterClass, clz -> {
            try {
                return clz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Failed to instantiate CmsFormatter class: {}", clz.getName(), e);
                return new DefaultFormatter();
            }
        });
    }

    /**
     * Retrieves all registered CMS types.
     *
     * @return A sorted list of all registered {@link CmsTypeMetadata}.
     */
    public List<CmsTypeMetadata> getAllTypes() {
        List<CmsTypeMetadata> list = new ArrayList<>(registry.values());
        list.sort(Comparator.comparing(CmsTypeMetadata::getDisplayName));
        return list;
    }

    /**
     * Looks up type metadata by its lower-case type code.
     *
     * @param code The lower-case type code (e.g. "product").
     * @return The registered {@link CmsTypeMetadata}, or null if not found.
     */
    public CmsTypeMetadata getTypeMetadata(String code) {
        if (code == null) {
            return null;
        }
        return registry.get(code.toLowerCase());
    }
}
