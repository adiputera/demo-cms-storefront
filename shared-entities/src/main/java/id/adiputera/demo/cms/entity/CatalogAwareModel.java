package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

/**
 * Catalog Aware Model class.
 *
 * @author Yusuf F. Adiputera
 */
@MappedSuperclass
@Getter
@Setter
public abstract class CatalogAwareModel extends ItemModel {

    @CmsField(
        displayName = "Catalog",
        type = CmsFieldType.REFERENCE,
        required = true,
        editableOnUpdate = false,
        order = 0,
        targetEntity = Catalog.class
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @Column(name = "sync_version", nullable = false)
    private Integer syncVersion = 1;

    /**
     * Callback method triggered before entity update.
     * Increments the sync version if catalog version is staged.
     *
     * @see ItemModel#onUpdate()
     */
    @Override
    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        if (catalog == null || catalog.getVersion() == CatalogVersion.STAGED) {
            if (syncVersion == null) {
                syncVersion = 1;
            } else {
                syncVersion++;
            }
        }
    }

    /**
     * Returns a unique business key that identifies this entity across different catalog versions.
     *
     * @return The unique business key.
     */
    public abstract String getSyncKey();

    /**
     * Returns the name of the entity field that corresponds to the sync key (e.g., "code", "uid", "slug").
     * Used for dynamic querying during reference serialization.
     *
     * @return The field name.
     */
    public abstract String getSyncKeyFieldName();
}
