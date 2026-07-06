package id.adiputera.demo.cms.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @Column(name = "sync_version", nullable = false)
    private Integer syncVersion = 1;

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
     */
    public abstract String getSyncKey();
}
