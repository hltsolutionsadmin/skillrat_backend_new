package com.skillrat.common.orm;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Instant getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
    
    public Instant getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(name = "created_date", nullable = true, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = true)
    private Instant updatedDate;

    @CreatedBy
    @Column(name = "created_by", nullable = true, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = true)
    private String updatedBy;

    @Column(name = "tenant_id", nullable = true)
    private String tenantId;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = Instant.now();
        }
        if (updatedDate == null) {
            updatedDate = createdDate;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = Instant.now();
    }
}
