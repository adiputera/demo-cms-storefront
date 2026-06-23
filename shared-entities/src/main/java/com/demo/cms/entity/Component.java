package com.demo.cms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "components", uniqueConstraints = {
    @UniqueConstraint(name = "uk_components_uid_catalog", columnNames = {"uid", "catalog_id"})
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Component extends CatalogAwareModel {

    @Override
    public String getSyncKey() {
        return getUid();
    }

    @NotBlank(message = "UID is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String uid;

    @NotBlank(message = "Component name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Component type is required")
    @Column(name = "type", nullable = false, length = 50)
    private String type;



    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        // Set type from subclass implementation if not already set
        if (type == null) {
            ComponentType componentType = getType();
            if (componentType != null) {
                type = componentType.name();
            }
        }
    }

    // Abstract method to get component type (implemented by subclasses)
    public abstract ComponentType getType();
}
