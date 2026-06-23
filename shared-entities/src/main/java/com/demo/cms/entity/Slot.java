package com.demo.cms.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "slots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_slots_page_code_catalog", columnNames = {"page_id", "code", "catalog_id"})
}, indexes = {
    @Index(name = "idx_slots_page_code", columnList = "page_id, code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot extends CatalogAwareModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Slot code is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String code;

    @NotBlank(message = "Slot name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "slot_components",
        joinColumns = @JoinColumn(name = "slot_id"),
        inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<Component> components = new ArrayList<>();

}
