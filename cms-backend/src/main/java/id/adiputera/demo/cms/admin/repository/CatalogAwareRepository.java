package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.CatalogAwareModel;
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
    Page<T> findAllByCatalog(Catalog catalog, Pageable pageable);
    long countByCatalog(Catalog catalog);
}
