package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Catalog Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    Optional<Catalog> findByCatalogIdAndVersion(String catalogId, CatalogVersion version);
}
