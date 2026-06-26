package id.adiputera.demo.cms.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.Catalog;
import java.util.Optional;

@Repository
public interface ComponentRepository extends CatalogAwareRepository<Component> {
    List<Component> findAllByCatalog(Catalog catalog);
    Optional<Component> findByUidAndCatalog(String uid, Catalog catalog);
}
