package com.skillrat.user.organisation.domain;

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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.UniqueConstraint;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "b2b_unit_departments",
            joinColumns = @JoinColumn(name = "b2b_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"b2b_unit_id", "department_id"})
    )
    private Set<Department> departments = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "onboarded_by_user_id")
    private com.skillrat.user.domain.User onboardedBy;

    @OneToOne
    @JoinColumn(name = "approved_by_user_id")
    private com.skillrat.user.domain.User approvedBy;

    private Instant approvedAt;
}
