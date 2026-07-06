package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Page Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface PageRepository extends CatalogAwareRepository<Page> {
    
    @Query("SELECT p FROM Page p LEFT JOIN FETCH p.breadcrumbs LEFT JOIN FETCH p.slots WHERE p.slug = :slug AND p.catalog = :catalog")
    Optional<Page> findBySlugWithRelations(@Param("slug") String slug, @Param("catalog") Catalog catalog);
    
    Optional<Page> findBySlugAndCatalog(String slug, Catalog catalog);
    
    boolean existsBySlugAndCatalog(String slug, Catalog catalog);
    
    List<Page> findAllByCatalog(Catalog catalog);
}
