package com.demo.cms.storefront.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.code = :code AND p.catalog.version = com.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    Optional<Product> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Product p WHERE p.code IN :codes AND p.catalog.version = com.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    List<Product> findByCodeIn(@Param("codes") List<String> codes);

    @Query("SELECT p FROM Product p WHERE p.catalog.version = com.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    List<Product> findAllOnline();
}
