package com.skillrat.user.organisation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skillrat.user.domain.EmployeeOrgBand;
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
    @JsonIgnoreProperties("b2bUnits")
    private Set<Department> departments = new HashSet<>();

    // Helper method to add Department
    public void addDepartment(Department department) {
        this.departments.add(department);
        department.getB2bUnits().add(this);
    }

    // Helper method to remove Department
    public void removeDepartment(Department department) {
        this.departments.remove(department);
        department.getB2bUnits().remove(this);
    }

    @OneToOne
    @JoinColumn(name = "onboarded_by_user_id")
    private com.skillrat.user.domain.User onboardedBy;

    @OneToOne
    @JoinColumn(name = "approved_by_user_id")
    private com.skillrat.user.domain.User approvedBy;

    private Instant approvedAt;

    @OneToMany(mappedBy = "b2bUnit", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<EmployeeOrgBand> employeeBands = new HashSet<>();
}
