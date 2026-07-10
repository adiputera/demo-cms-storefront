package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.dto.CmsRowDTO;
import id.adiputera.demo.cms.admin.exception.BadRequestException;
import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.metadata.CmsTypeMetadata;
import id.adiputera.demo.cms.admin.metadata.CmsTypeRegistry;
import id.adiputera.demo.cms.entity.ItemModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service orchestrating generic CRUD operations for all CMS registered models.
 * Completely metadata-driven and decouples entities from specific DTO mappings.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CmsCrudService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final CmsTypeRegistry cmsTypeRegistry;
    private final GenericEntityMapper genericEntityMapper;
    private final CmsValidator cmsValidator;

    /**
     * Retrieves an entity by its lower-case type code and primary identifier.
     *
     * @param type The lower-case model type code.
     * @param id The primary database identifier.
     * @return The populated tabular DTO representation.
     * @throws ResourceNotFoundException If type or entity identifier is not found.
     */
    public CmsRowDTO getEntityById(String type, String id) {
        CmsTypeMetadata typeMeta = cmsTypeRegistry.getTypeMetadata(type);
        if (typeMeta == null) {
            throw new ResourceNotFoundException("CMS Type not found: " + type);
        }
        ItemModel entity = (ItemModel) entityManager.find(typeMeta.getEntityClass(), Long.valueOf(id));
        if (entity == null) {
            throw new ResourceNotFoundException(typeMeta.getDisplayName() + " with ID " + id + " not found");
        }
        return genericEntityMapper.mapToRow(entity, typeMeta);
    }

    /**
     * Creates a new entity instance dynamically based on registration type and input payload.
     *
     * @param type The lower-case model type code.
     * @param payload The raw JSON fields input map.
     * @return The populated tabular DTO representation of the persisted entity.
     * @throws BadRequestException If validations or instantiation fails.
     */
    @Transactional
    public CmsRowDTO createEntity(String type, Map<String, Object> payload) {
        CmsTypeMetadata typeMeta = cmsTypeRegistry.getTypeMetadata(type);
        if (typeMeta == null) {
            throw new ResourceNotFoundException("CMS Type not found: " + type);
        }
        cmsValidator.validate(payload, typeMeta, true);
        try {
            ItemModel entity = (ItemModel) typeMeta.getEntityClass().getDeclaredConstructor().newInstance();
            genericEntityMapper.populateEntity(entity, payload, typeMeta, true);
            entityManager.persist(entity);
            entityManager.flush();
            return genericEntityMapper.mapToRow(entity, typeMeta);
        } catch (Exception e) {
            log.error("Failed to create entity of type {}", type, e);
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new BadRequestException("Failed to create " + typeMeta.getDisplayName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Modifies an existing entity dynamically based on registration type and input fields.
     *
     * @param type The lower-case model type code.
     * @param id The primary database identifier.
     * @param payload The raw JSON fields update input map.
     * @return The populated tabular DTO representation of the updated entity.
     * @throws BadRequestException If validations or database updates fail.
     * @throws ResourceNotFoundException If target entity is not found.
     */
    @Transactional
    public CmsRowDTO updateEntity(String type, String id, Map<String, Object> payload) {
        CmsTypeMetadata typeMeta = cmsTypeRegistry.getTypeMetadata(type);
        if (typeMeta == null) {
            throw new ResourceNotFoundException("CMS Type not found: " + type);
        }
        ItemModel entity = (ItemModel) entityManager.find(typeMeta.getEntityClass(), Long.valueOf(id));
        if (entity == null) {
            throw new ResourceNotFoundException(typeMeta.getDisplayName() + " with ID " + id + " not found");
        }
        cmsValidator.validate(payload, typeMeta, false);
        try {
            genericEntityMapper.populateEntity(entity, payload, typeMeta, false);
            ItemModel merged = entityManager.merge(entity);
            entityManager.flush();
            return genericEntityMapper.mapToRow(merged, typeMeta);
        } catch (Exception e) {
            log.error("Failed to update entity of type {} with id {}", type, id, e);
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new BadRequestException("Failed to update " + typeMeta.getDisplayName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a registered CMS entity instance by identifier.
     *
     * @param type The lower-case model type code.
     * @param id The primary database identifier.
     * @throws ResourceNotFoundException If target entity is not found.
     */
    @Transactional
    public void deleteEntity(String type, String id) {
        CmsTypeMetadata typeMeta = cmsTypeRegistry.getTypeMetadata(type);
        if (typeMeta == null) {
            throw new ResourceNotFoundException("CMS Type not found: " + type);
        }
        ItemModel entity = (ItemModel) entityManager.find(typeMeta.getEntityClass(), Long.valueOf(id));
        if (entity == null) {
            throw new ResourceNotFoundException(typeMeta.getDisplayName() + " with ID " + id + " not found");
        }
        entityManager.remove(entity);
        entityManager.flush();
    }
}
