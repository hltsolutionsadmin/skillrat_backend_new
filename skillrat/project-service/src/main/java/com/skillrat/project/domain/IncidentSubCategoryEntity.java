package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "incident_sub_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_incident_sub_category_tenant_cat_code", columnNames = {"tenant_id", "category_id", "code"})
        })
@Audited
@Getter
@Setter
@NoArgsConstructor
public class IncidentSubCategoryEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private IncidentCategoryEntity category;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
