package com.skillrat.organisation.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;

@Entity
@Table(
        name = "b2b_unit",
        indexes = {
                @Index(name = "idx_b2b_name_tenant", columnList = "name, tenant_id"),
                @Index(name = "idx_b2b_status", columnList = "status"),
                @Index(name = "idx_b2b_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class B2BUnit extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private B2BUnitType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private B2BUnitStatus status = B2BUnitStatus.PENDING_APPROVAL;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 32)
    private String contactPhone;

    @Column(length = 255)
    private String website;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", unique = true)
    private Address address;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private B2BGroup group;

    @Column(length = 32)
    private String onboardedBy; // SELF or ADMIN

    @Column(length = 100)
    private String approvedBy;

    private Instant approvedAt;
}
