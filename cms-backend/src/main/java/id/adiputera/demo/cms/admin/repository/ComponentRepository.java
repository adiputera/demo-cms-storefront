package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Component Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface ComponentRepository extends CatalogAwareRepository<Component> {
    List<Component> findAllByCatalog(Catalog catalog);
    Optional<Component> findByUidAndCatalog(String uid, Catalog catalog);
}
