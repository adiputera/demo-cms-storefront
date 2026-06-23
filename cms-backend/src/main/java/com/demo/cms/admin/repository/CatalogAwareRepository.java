package com.demo.cms.admin.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.demo.cms.entity.Catalog;
import com.demo.cms.entity.CatalogAwareModel;

@NoRepositoryBean
public interface CatalogAwareRepository<T extends CatalogAwareModel> extends JpaRepository<T, Long> {
    Page<T> findAllByCatalog(Catalog catalog, Pageable pageable);
}
