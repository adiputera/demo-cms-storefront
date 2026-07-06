package id.adiputera.demo.cms.entity;

import id.adiputera.demo.cms.dto.ItemSearchResultDTO;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Item Model class.
 *
 * @author Yusuf F. Adiputera
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ItemSearchResultDTO toItemSearchResultDTO() {
        return new ItemSearchResultDTO(
                id != null ? String.valueOf(id) : null,
                this.getClass().getSimpleName(),
                createdAt != null ? createdAt.toString() : null
        );
    }
}
