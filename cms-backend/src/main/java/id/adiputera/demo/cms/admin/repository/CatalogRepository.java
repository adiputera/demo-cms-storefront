package id.adiputera.demo.cms.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogVersion;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    Optional<Catalog> findByCatalogIdAndVersion(String catalogId, CatalogVersion version);
}
