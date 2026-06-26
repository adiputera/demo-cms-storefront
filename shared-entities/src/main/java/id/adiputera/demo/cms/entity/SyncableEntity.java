package id.adiputera.demo.cms.entity;

public interface SyncableEntity {
    /**
     * Returns a unique business key that identifies this entity across different catalog versions.
     * (e.g., Slug for a Page, UID for a Component, Code + Page for a Slot)
     */
    String getSyncKey();
}
