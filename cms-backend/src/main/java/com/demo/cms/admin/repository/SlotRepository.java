package com.demo.cms.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Slot;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    
    @Query("SELECT s FROM Slot s LEFT JOIN FETCH s.components WHERE s.id = :id")
    Optional<Slot> findByIdWithComponents(Long id);
    
    @Query("SELECT s FROM Slot s WHERE s.page.id = :pageId")
    List<Slot> findByPageId(Long pageId);
    
    boolean existsByCodeAndPageId(String code, Long pageId);
}
