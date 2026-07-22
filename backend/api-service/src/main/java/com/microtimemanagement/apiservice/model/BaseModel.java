package com.microtimemanagement.apiservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * Audit base for every entity. {@code createdAt} / {@code lastUpdatedAt} /
 * {@code isActive} are maintained by JPA lifecycle callbacks here, replacing
 * the old Mongo {@code BeforeConvertCallback} classes.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseModel {

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt;

    @Column(name = "last_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastUpdatedAt;

    @PrePersist
    void onCreate() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        lastUpdatedAt = now;
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }

    @PreUpdate
    void onUpdate() {
        lastUpdatedAt = new Date();
    }
}
