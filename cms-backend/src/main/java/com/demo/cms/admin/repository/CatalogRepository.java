package com.demo.cms.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Catalog;
import com.demo.cms.entity.CatalogVersion;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    Optional<Catalog> findByCatalogIdAndVersion(String catalogId, CatalogVersion version);
}
