package id.adiputera.demo.cms.storefront.repository;

import id.adiputera.demo.cms.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Slot Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("SELECT DISTINCT s FROM Slot s " +
           "LEFT JOIN FETCH s.components c " +
           "WHERE s.id IN :slotIds " +
           "ORDER BY s.id")
    List<Slot> findByIdInWithComponents(@Param("slotIds") List<Long> slotIds);
}
