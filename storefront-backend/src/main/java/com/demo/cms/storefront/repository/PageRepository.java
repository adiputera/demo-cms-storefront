package com.demo.cms.storefront.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Page;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    @Query("SELECT p FROM Page p " +
           "LEFT JOIN FETCH p.breadcrumbs " +
           "LEFT JOIN FETCH p.slots " +
           "WHERE p.slug = :slug")
    Optional<Page> findBySlugWithRelations(@Param("slug") String slug);

    Optional<Page> findBySlug(String slug);
}
