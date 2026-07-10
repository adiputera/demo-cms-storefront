package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogAwareModel;
import id.adiputera.demo.cms.entity.CatalogVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Catalog Aware Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@NoRepositoryBean
public interface CatalogAwareRepository<T extends CatalogAwareModel> extends JpaRepository<T, Long> {
    /**
     * Finds all entities belonging to the given catalog.
     *
     * @param catalog  The catalog to filter by.
     * @param pageable Pagination parameters.
     * @return A page of matching entities.
     */
    Page<T> findAllByCatalog(Catalog catalog, Pageable pageable);

    /**
     * Finds all entities whose catalog has the given version (across all catalog IDs).
     * Useful for loading all ONLINE entities of a type regardless of which named catalog they belong to.
     *
     * @param version  The catalog version to filter by (e.g. ONLINE).
     * @param pageable Pagination parameters.
     * @return A page of matching entities.
     */
    Page<T> findAllByCatalogVersion(CatalogVersion version, Pageable pageable);

    /**
     * Counts entities belonging to the given catalog.
     *
     * @param catalog The catalog to count within.
     * @return The count of matching entities.
     */
    long countByCatalog(Catalog catalog);
}

