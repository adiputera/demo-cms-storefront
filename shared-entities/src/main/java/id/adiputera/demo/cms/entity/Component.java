package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Component class.
 *
 * @author Yusuf F. Adiputera
 */
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

    @Override
    public String getSyncKeyFieldName() {
        return "uid";
    }

    @NotBlank(message = "UID is required")
    @Size(max = 100)
    @Column(nullable = false)
    @CmsField(
        displayName = "Component UID",
        type = CmsFieldType.STRING,
        required = true,
        editableOnUpdate = false,
        searchable = true,
        order = 1
    )
    private String uid;

    @NotBlank(message = "Component name is required")
    @Size(max = 255)
    @Column(nullable = false)
    @CmsField(
        displayName = "Component Name",
        type = CmsFieldType.STRING,
        required = true,
        searchable = true,
        order = 2
    )
    private String name;

    @NotNull(message = "Component type is required")
    @Column(name = "type", nullable = false, length = 50)
    @CmsField(
        displayName = "Component Type",
        type = CmsFieldType.STRING,
        required = false,
        editableOnUpdate = false,
        searchable = true,
        order = 3
    )
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
