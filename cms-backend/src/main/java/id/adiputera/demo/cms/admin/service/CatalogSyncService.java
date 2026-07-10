package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.repository.CatalogAwareRepository;
import id.adiputera.demo.cms.admin.repository.CatalogRepository;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.annotation.ReferenceCardinality;
import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogAwareModel;
import id.adiputera.demo.cms.entity.CatalogVersion;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Catalog Sync Service class.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSyncService {

    private final CatalogRepository catalogRepository;
    private final EntityManager entityManager;
    private final ApplicationContext applicationContext;
    private final StorefrontCacheEvictionService storefrontCacheEvictionService;
    private final PlatformTransactionManager transactionManager;

    private Repositories repositories;
    private TransactionTemplate transactionTemplate;
    private List<Class<? extends CatalogAwareModel>> sortedEntityClasses = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.repositories = new Repositories(applicationContext);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        buildTopologicalSort();
    }

    private void buildTopologicalSort() {
        Set<Class<? extends CatalogAwareModel>> nodes = new HashSet<>();
        Map<Class<? extends CatalogAwareModel>, Set<Class<? extends CatalogAwareModel>>> adjList = new HashMap<>();
        Map<Class<? extends CatalogAwareModel>, Integer> inDegree = new HashMap<>();

        // 1. Discover nodes
        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            if (CatalogAwareModel.class.isAssignableFrom(javaType)) {
                @SuppressWarnings("unchecked")
                Class<? extends CatalogAwareModel> modelClass = (Class<? extends CatalogAwareModel>) javaType;
                nodes.add(modelClass);
                adjList.put(modelClass, new HashSet<>());
                inDegree.put(modelClass, 0);
            }
        }

        // 2. Build edges (dependencies)
        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            if (!CatalogAwareModel.class.isAssignableFrom(javaType)) continue;
            
            @SuppressWarnings("unchecked")
            Class<? extends CatalogAwareModel> dependent = (Class<? extends CatalogAwareModel>) javaType;

            for (Attribute<?, ?> attr : entityType.getAttributes()) {
                if (attr.isAssociation()) {
                    // Ignore ONE_TO_MANY as they are usually the inverse side of a MANY_TO_ONE
                    if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY) {
                        continue;
                    }

                    Class<?> targetType;
                    if (attr.isCollection()) {
                        targetType = ((PluralAttribute<?, ?, ?>) attr).getElementType().getJavaType();
                    } else {
                        targetType = attr.getJavaType();
                    }

                    if (CatalogAwareModel.class.isAssignableFrom(targetType) && !targetType.equals(dependent)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends CatalogAwareModel> dependency = (Class<? extends CatalogAwareModel>) targetType;
                        
                        // dependent relies on dependency. So dependency must be processed FIRST.
                        // Directed edge: dependency -> dependent
                        if (adjList.get(dependency).add(dependent)) {
                            inDegree.put(dependent, inDegree.get(dependent) + 1);
                        }
                    }
                }
            }
        }

        // 3. Kahn's Algorithm
        Queue<Class<? extends CatalogAwareModel>> queue = new LinkedList<>();
        for (Class<? extends CatalogAwareModel> node : nodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        while (!queue.isEmpty()) {
            Class<? extends CatalogAwareModel> u = queue.poll();
            sortedEntityClasses.add(u);
            
            for (Class<? extends CatalogAwareModel> v : adjList.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        if (sortedEntityClasses.size() != nodes.size()) {
            log.error("Cycle detected in CatalogAwareModel dependencies! Automatic topological sort failed. Synced {} out of {} entities.", sortedEntityClasses.size(), nodes.size());
            throw new IllegalStateException("Circular dependencies found in CatalogAware models.");
        }

        log.info("Catalog Sync Entity Order Resolved: {}", sortedEntityClasses.stream().map(Class::getSimpleName).toList());
    }

    public void syncCatalog(String catalogId) {
        log.info("Starting universal sync for catalog: {}", catalogId);

        Catalog stagedCatalog = catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.STAGED)
            .orElseThrow(() -> new IllegalArgumentException("Staged catalog not found: " + catalogId));

        Catalog onlineCatalog = catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.ONLINE)
            .orElseGet(() -> catalogRepository.save(Catalog.builder()
                .catalogId(catalogId)
                .version(CatalogVersion.ONLINE)
                .build()));

        // Global Cache to store synced entities across tables. O(1) relational lookups!
        Map<Class<?>, Map<String, CatalogAwareModel>> syncedCache = new HashMap<>();

        for (Class<? extends CatalogAwareModel> entityClass : sortedEntityClasses) {
            syncEntityClass(entityClass, stagedCatalog, onlineCatalog, syncedCache);
        }

        storefrontCacheEvictionService.evictStorefrontCaches();
        log.info("Universal catalog sync completed successfully for: {}", catalogId);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void syncSingleItem(String entityType, Long itemId) {
        log.info("Starting single item sync for {} with ID: {}", entityType, itemId);

        Class<? extends CatalogAwareModel> targetClass = null;
        for (Class<? extends CatalogAwareModel> cls : sortedEntityClasses) {
            if (cls.getSimpleName().equalsIgnoreCase(entityType)) {
                targetClass = cls;
                break;
            }
        }

        if (targetClass == null) {
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }

        CatalogAwareRepository<CatalogAwareModel> repo = (CatalogAwareRepository<CatalogAwareModel>) repositories.getRepositoryFor(targetClass)
                .orElseThrow(() -> new IllegalStateException("No repository found for " + entityType));

        CatalogAwareModel stagedEntity = repo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

        if (stagedEntity.getCatalog().getVersion() != CatalogVersion.STAGED) {
            throw new IllegalArgumentException("Item must belong to a STAGED catalog");
        }

        Catalog stagedCatalog = stagedEntity.getCatalog();
        Catalog onlineCatalog = catalogRepository.findByCatalogIdAndVersion(stagedCatalog.getCatalogId(), CatalogVersion.ONLINE)
                .orElseThrow(() -> new IllegalArgumentException("Online catalog not found for: " + stagedCatalog.getCatalogId()));

        Map<Class<?>, Map<String, CatalogAwareModel>> syncedCache = new HashMap<>();

        // Prime the cache with the online versions of this entity type
        List<CatalogAwareModel> onlineRecords = repo.findAllByCatalog(onlineCatalog, Pageable.unpaged()).getContent();
        Map<String, CatalogAwareModel> typeCache = new HashMap<>();
        for (CatalogAwareModel m : onlineRecords) {
            typeCache.put(m.getSyncKey(), m);
        }
        syncedCache.put(targetClass, typeCache);

        CatalogAwareModel onlineEntity = typeCache.get(stagedEntity.getSyncKey());
        if (onlineEntity == null) {
            onlineEntity = instantiateEntity(stagedEntity);
        }

        copySimpleProperties(stagedEntity, onlineEntity, targetClass);
        onlineEntity.setCatalog(onlineCatalog);

        // No translation needed for string reference fields, syncKeys are copied verbatim

        resolveRelationships(stagedEntity, onlineEntity, targetClass, syncedCache, onlineCatalog);

        repo.save(onlineEntity);
        storefrontCacheEvictionService.evictStorefrontCaches();
        log.info("Single item sync completed for {} with ID: {}", entityType, itemId);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T extends CatalogAwareModel> Map<String, String> calculateSyncStatus(
            List<T> stagedItems, Class<T> entityClass, String catalogId) {
        Map<String, String> statusMap = new HashMap<>();
        if (stagedItems.isEmpty()) return statusMap;

        Optional<Object> repoOpt = repositories.getRepositoryFor(entityClass);
        if (repoOpt.isEmpty()) {
            stagedItems.forEach(item -> statusMap.put(item.getSyncKey(), "UNKNOWN"));
            return statusMap;
        }

        CatalogAwareRepository<T> repo = (CatalogAwareRepository<T>) repoOpt.get();

        Catalog onlineCatalog = catalogRepository.findByCatalogIdAndVersion(catalogId, CatalogVersion.ONLINE)
                .orElse(null);

        if (onlineCatalog == null) {
            stagedItems.forEach(item -> statusMap.put(item.getSyncKey(), "NOT_SYNCED"));
            return statusMap;
        }

        List<T> onlineRecords = repo.findAllByCatalog(onlineCatalog, Pageable.unpaged()).getContent();
        Map<String, T> onlineMap = new HashMap<>();
        for (T o : onlineRecords) {
            onlineMap.put(o.getSyncKey(), o);
        }

        for (T staged : stagedItems) {
            T online = onlineMap.get(staged.getSyncKey());
            if (online == null) {
                statusMap.put(staged.getSyncKey(), "NOT_SYNCED");
            } else if (!staged.getSyncVersion().equals(online.getSyncVersion())) {
                statusMap.put(staged.getSyncKey(), "OUT_OF_SYNC");
            } else {
                statusMap.put(staged.getSyncKey(), "SYNCED");
            }
        }
        return statusMap;
    }

    @Transactional(readOnly = true)
    public <T extends CatalogAwareModel> Map<String, String> calculateSyncStatus(List<T> stagedItems, Class<T> entityClass) {
        if (stagedItems.isEmpty()) return new HashMap<>();
        // Force-initialize the lazy catalog proxy so getCatalogId() is safe outside a session
        org.hibernate.Hibernate.initialize(stagedItems.get(0).getCatalog());
        String catalogId = stagedItems.get(0).getCatalog() != null
                ? stagedItems.get(0).getCatalog().getCatalogId()
                : "contentCatalog";
        return calculateSyncStatus(stagedItems, entityClass, catalogId);
    }

    @SuppressWarnings("unchecked")
    private <T extends CatalogAwareModel> void syncEntityClass(
            Class<T> entityClass, 
            Catalog stagedCatalog, 
            Catalog onlineCatalog,
            Map<Class<?>, Map<String, CatalogAwareModel>> syncedCache) {
        
        Optional<Object> repoOpt = repositories.getRepositoryFor(entityClass);
        if (repoOpt.isEmpty()) {
            log.warn("No repository found for entity {}", entityClass.getSimpleName());
            return;
        }

        CatalogAwareRepository<T> repo = (CatalogAwareRepository<T>) repoOpt.get();
        Map<String, CatalogAwareModel> entityCache = syncedCache.computeIfAbsent(entityClass, k -> new HashMap<>());

        int page = 0;
        int batchSize = 500;
        boolean hasNext;
        long totalSynced = 0;

        do {
            final int currentPage = page;
            org.springframework.data.domain.Page<T> stagedBatch = transactionTemplate.execute(status -> {
                Pageable pageable = PageRequest.of(currentPage, batchSize);
                org.springframework.data.domain.Page<T> batch = repo.findAllByCatalog(stagedCatalog, pageable);

                // Pre-fetch all ONLINE matching entities to avoid N+1 queries during the batch
                List<T> onlineBatchFetch = repo.findAllByCatalog(onlineCatalog, Pageable.unpaged()).getContent();
                Map<String, T> onlineExistingMap = new HashMap<>();
                for (T o : onlineBatchFetch) {
                    onlineExistingMap.put(o.getSyncKey(), o);
                }

                for (T stagedEntity : batch.getContent()) {
                    T onlineEntity = onlineExistingMap.get(stagedEntity.getSyncKey());
                    if (onlineEntity == null) {
                        onlineEntity = instantiateEntity(stagedEntity);
                    }

                    // 1. Copy simple properties (BeanUtils ignores associations via Metamodel)
                    copySimpleProperties(stagedEntity, onlineEntity, entityClass);

                    // 2. Set Catalog
                    onlineEntity.setCatalog(onlineCatalog);

                    // 3. No translation needed for string reference fields, syncKeys are copied verbatim

                    // 4. Resolve Relationships using Metamodel and Cache
                    resolveRelationships(stagedEntity, onlineEntity, entityClass, syncedCache, onlineCatalog);

                    // 5. Save and add to global cache for downstream dependents
                    onlineEntity = repo.save(onlineEntity);
                    entityCache.put(onlineEntity.getSyncKey(), onlineEntity);
                }

                entityManager.flush();
                entityManager.clear();
                return batch;
            });

            if (stagedBatch != null) {
                totalSynced += stagedBatch.getNumberOfElements();
                hasNext = stagedBatch.hasNext();
            } else {
                hasNext = false;
            }
            page++;

        } while (hasNext);
        
        log.info("Synced {} records for {}", totalSynced, entityClass.getSimpleName());
    }

    private <T> void copySimpleProperties(T source, T target, Class<?> entityClass) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        List<String> ignoredProperties = new ArrayList<>(List.of("id", "catalog", "createdAt", "updatedAt", "syncVersion"));
        for (Attribute<?, ?> attr : entityType.getAttributes()) {
            if (attr.isAssociation() || attr.isCollection()) {
                ignoredProperties.add(attr.getName());
            }
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoredProperties.toArray(new String[0]));
        
        // Explicitly copy syncVersion so the ONLINE version perfectly matches the STAGED version at the time of sync
        if (source instanceof CatalogAwareModel && target instanceof CatalogAwareModel) {
            ((CatalogAwareModel) target).setSyncVersion(((CatalogAwareModel) source).getSyncVersion());
        }
    }

    private <T extends CatalogAwareModel> void resolveRelationships(
            T staged, T online, Class<?> entityClass, Map<Class<?>, Map<String, CatalogAwareModel>> cache, Catalog onlineCatalog) {
        
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        BeanWrapper stagedWrapper = new BeanWrapperImpl(staged);
        BeanWrapper onlineWrapper = new BeanWrapperImpl(online);

        for (Attribute<?, ?> attr : entityType.getAttributes()) {
            if (!attr.isAssociation()) continue;

            String propName = attr.getName();
            Object stagedVal = stagedWrapper.getPropertyValue(propName);
            if (stagedVal == null) {
                onlineWrapper.setPropertyValue(propName, null);
                continue;
            }

            if (attr.isCollection()) {
                // Handle collections
                Class<?> targetType = ((PluralAttribute<?, ?, ?>) attr).getElementType().getJavaType();
                if (!CatalogAwareModel.class.isAssignableFrom(targetType)) continue;

                @SuppressWarnings("unchecked")
                Collection<CatalogAwareModel> stagedCol = (Collection<CatalogAwareModel>) stagedVal;
                
                @SuppressWarnings("unchecked")
                Collection<Object> existingCol = (Collection<Object>) onlineWrapper.getPropertyValue(propName);
                if (existingCol == null) {
                    if (List.class.isAssignableFrom(attr.getJavaType())) {
                        existingCol = new ArrayList<>();
                    } else {
                        existingCol = new HashSet<>();
                    }
                    onlineWrapper.setPropertyValue(propName, existingCol);
                }

                existingCol.clear();
                for (CatalogAwareModel stagedItem : stagedCol) {
                    CatalogAwareModel onlineItem = getFromCache(targetType, stagedItem.getSyncKey(), cache, onlineCatalog);
                    if (onlineItem != null) {
                        existingCol.add(onlineItem);
                    }
                }

            } else {
                // Handle single association
                Class<?> targetType = attr.getJavaType();
                if (!CatalogAwareModel.class.isAssignableFrom(targetType)) continue;

                CatalogAwareModel stagedRef = (CatalogAwareModel) stagedVal;
                CatalogAwareModel onlineRef = getFromCache(targetType, stagedRef.getSyncKey(), cache, onlineCatalog);
                if (onlineRef != null) {
                    onlineWrapper.setPropertyValue(propName, onlineRef);
                }
            }
        }
    }

    private CatalogAwareModel getFromCache(Class<?> targetType, String syncKey, Map<Class<?>, Map<String, CatalogAwareModel>> cache, Catalog onlineCatalog) {
        Map<String, CatalogAwareModel> typeCache = cache.get(targetType);
        
        if (typeCache == null) {
            log.info("Lazy-loading ONLINE entities for {} into cache", targetType.getSimpleName());
            Optional<Object> repoOpt = repositories.getRepositoryFor(targetType);
            typeCache = new HashMap<>();
            
            if (repoOpt.isPresent()) {
                @SuppressWarnings("unchecked")
                CatalogAwareRepository<CatalogAwareModel> targetRepo = (CatalogAwareRepository<CatalogAwareModel>) repoOpt.get();
                List<CatalogAwareModel> onlineRecords = targetRepo.findAllByCatalog(onlineCatalog, Pageable.unpaged()).getContent();
                for (CatalogAwareModel m : onlineRecords) {
                    typeCache.put(m.getSyncKey(), m);
                }
            }
            cache.put(targetType, typeCache);
        }
        
        if (typeCache.containsKey(syncKey)) {
            return typeCache.get(syncKey);
        }
        
        // Polymorphic check: If looking for 'Component', check 'BannerComponent' caches too
        for (Map.Entry<Class<?>, Map<String, CatalogAwareModel>> entry : cache.entrySet()) {
            if (targetType.isAssignableFrom(entry.getKey())) {
                CatalogAwareModel model = entry.getValue().get(syncKey);
                if (model != null) return model;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiateEntity(T staged) {
        try {
            // Need to handle hibernate proxies, get actual class
            Class<?> clazz = org.hibernate.Hibernate.getClass(staged);
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate entity subclass: " + staged.getClass(), e);
        }
    }

}
