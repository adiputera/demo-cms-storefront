package com.demo.cms.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Page;
import com.demo.cms.entity.Catalog;
import org.springframework.data.repository.query.Param;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    
    @Query("SELECT p FROM Page p LEFT JOIN FETCH p.breadcrumbs LEFT JOIN FETCH p.slots WHERE p.slug = :slug AND p.catalog = :catalog")
    Optional<Page> findBySlugWithRelations(@Param("slug") String slug, @Param("catalog") Catalog catalog);
    
    Optional<Page> findBySlugAndCatalog(String slug, Catalog catalog);
    
    boolean existsBySlugAndCatalog(String slug, Catalog catalog);
    
    List<Page> findAllByCatalog(Catalog catalog);
}
