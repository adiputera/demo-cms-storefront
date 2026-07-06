package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Slot;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Slot Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface SlotRepository extends CatalogAwareRepository<Slot> {
    
    @Query("SELECT s FROM Slot s LEFT JOIN FETCH s.components WHERE s.id = :id")
    Optional<Slot> findByIdWithComponents(@Param("id") Long id);
    
    @Query("SELECT s FROM Slot s WHERE s.page.id = :pageId")
    List<Slot> findByPageId(@Param("pageId") Long pageId);
    
    boolean existsByCodeAndPageId(String code, Long pageId);
    
    List<Slot> findAllByCatalog(Catalog catalog);
    
    Optional<Slot> findByCodeAndPageAndCatalog(String code, id.adiputera.demo.cms.entity.Page page, Catalog catalog);
}
