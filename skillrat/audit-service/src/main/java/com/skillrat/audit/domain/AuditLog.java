package com.skillrat.audit.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String serviceName;

    @Column(nullable = false, length = 128)
    private String entityType;

    @Column(nullable = false, length = 36)
    private String entityId;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 128)
    private String fieldName;

    @Column(length = 4000)
    private String oldValue;

    @Column(length = 4000)
    private String newValue;

    @Column(nullable = false, length = 255)
    private String changedBy;

    @Column(nullable = false)
    private java.time.Instant changedAt;
}
