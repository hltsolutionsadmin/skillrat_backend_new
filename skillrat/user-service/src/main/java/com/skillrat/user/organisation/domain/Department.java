package com.skillrat.user.organisation.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(
    name = "departments",
    indexes = {
        @Index(name = "idx_dept_name_tenant", columnList = "name, tenant_id"),
        @Index(name = "idx_dept_code_tenant", columnList = "code, tenant_id"),
        @Index(name = "idx_dept_active_tenant", columnList = "active, tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Department extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;

    @ManyToMany(mappedBy = "departments")
    private Set<B2BUnit> b2bUnits = new HashSet<>();

    @Column(nullable = false, length = 100)
    private String code;
}
