package com.demo.cms.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.cms.entity.Component;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
}
