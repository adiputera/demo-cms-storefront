package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Product Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface ProductRepository extends CatalogAwareRepository<Product> {
    
    Optional<Product> findByCodeAndCatalog(String code, Catalog catalog);
    
    boolean existsByCodeAndCatalog(String code, Catalog catalog);
}
