package id.adiputera.demo.cms.storefront.repository;

import id.adiputera.demo.cms.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.code = :code AND p.catalog.version = id.adiputera.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    Optional<Product> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Product p WHERE p.code IN :codes AND p.catalog.version = id.adiputera.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    List<Product> findByCodeIn(@Param("codes") List<String> codes);

    /**
     * Finds online products by their IDs.
     *
     * @param ids The list of product IDs.
     * @return The list of matching online products.
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.catalog.version = id.adiputera.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    List<Product> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Product p WHERE p.catalog.version = id.adiputera.demo.cms.entity.CatalogVersion.ONLINE AND p.catalog.catalogId = 'productCatalog'")
    List<Product> findAllOnline();
}
