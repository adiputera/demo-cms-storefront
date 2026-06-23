package com.demo.cms.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Slot;
import com.demo.cms.entity.Catalog;
import org.springframework.data.repository.query.Param;

@Repository
public interface SlotRepository extends CatalogAwareRepository<Slot> {
    
    @Query("SELECT s FROM Slot s LEFT JOIN FETCH s.components WHERE s.id = :id")
    Optional<Slot> findByIdWithComponents(@Param("id") Long id);
    
    @Query("SELECT s FROM Slot s WHERE s.page.id = :pageId")
    List<Slot> findByPageId(@Param("pageId") Long pageId);
    
    boolean existsByCodeAndPageId(String code, Long pageId);
    
    List<Slot> findAllByCatalog(Catalog catalog);
    
    Optional<Slot> findByCodeAndPageAndCatalog(String code, com.demo.cms.entity.Page page, Catalog catalog);
}
