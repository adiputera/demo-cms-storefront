package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.ComponentField;
import id.adiputera.demo.cms.admin.dto.ComponentSchema;
import id.adiputera.demo.cms.admin.dto.ComponentTypeInfo;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component Schema Service class.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentSchemaService {

    private final EntityManager entityManager;

    private final List<ComponentTypeInfo> typeInfos = new ArrayList<>();
    private final Map<String, ComponentSchema> schemaMap = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing ComponentSchemaService...");
        
        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            
            if (Component.class.isAssignableFrom(javaType) && javaType.isAnnotationPresent(CmsComponent.class)) {
                try {
                    CmsComponent componentMetadata = javaType.getAnnotation(CmsComponent.class);
                    
                    // Instantiate to get the specific type enum
                    Component instance = (Component) javaType.getConstructor().newInstance();
                    String typeCode = instance.getType().name();
                    
                    // Build Type Info
                    ComponentTypeInfo typeInfo = new ComponentTypeInfo(
                            typeCode,
                            componentMetadata.displayName(),
                            componentMetadata.description()
                    );
                    typeInfos.add(typeInfo);
                    
                    // Build Schema Fields
                    List<ComponentField> fields = new ArrayList<>();
                    for (Field field : javaType.getDeclaredFields()) {
                        if (field.isAnnotationPresent(CmsField.class)) {
                            CmsField fieldMetadata = field.getAnnotation(CmsField.class);
                            fields.add(new ComponentField(
                                    field.getName(),
                                    fieldMetadata.displayName(),
                                    fieldMetadata.type(),
                                    fieldMetadata.required(),
                                    fieldMetadata.placeholder()
                            ));
                        }
                    }
                    
                    ComponentSchema schema = new ComponentSchema(
                            typeCode,
                            componentMetadata.displayName(),
                            fields
                    );
                    schemaMap.put(typeCode, schema);
                    
                    log.info("Registered CMS Component: {}", typeCode);
                } catch (Exception e) {
                    log.error("Failed to process component schema for class {}", javaType.getName(), e);
                }
            }
        }
        
        // Sort types by displayName alphabetically
        typeInfos.sort(Comparator.comparing(ComponentTypeInfo::getDisplayName));
        
        log.info("Registered {} CMS Components.", typeInfos.size());
    }

    public List<ComponentTypeInfo> getComponentTypes() {
        return typeInfos;
    }

    public ComponentSchema getComponentSchema(String type) {
        ComponentSchema schema = schemaMap.get(type.toUpperCase());
        if (schema == null) {
            throw new ResourceNotFoundException("Schema not found for component type: " + type);
        }
        return schema;
    }
}
