package com.demo.cms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "catalogs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"catalog_id", "version"})
    },
    indexes = {
        @Index(name = "idx_catalog_id_version", columnList = "catalog_id, version")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Catalog ID is required")
    @Column(name = "catalog_id", nullable = false, length = 100)
    private String catalogId;

    @NotNull(message = "Version is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "version", nullable = false, length = 20)
    private CatalogVersion version;
}
