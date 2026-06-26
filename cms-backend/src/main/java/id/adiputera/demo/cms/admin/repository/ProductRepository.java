package id.adiputera.demo.cms.admin.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Product;

@Repository
public interface ProductRepository extends CatalogAwareRepository<Product> {
    
    Optional<Product> findByCodeAndCatalog(String code, Catalog catalog);
    
    boolean existsByCodeAndCatalog(String code, Catalog catalog);
}
