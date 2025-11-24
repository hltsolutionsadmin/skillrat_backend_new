package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incident_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_incident_category_tenant_org_code", columnNames = {"tenant_id", "organisation_id", "code"})
        })
@Audited
@Getter
@Setter
@NoArgsConstructor
public class IncidentCategoryEntity extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @OneToMany(mappedBy = "category")
    private List<IncidentSubCategoryEntity> subCategories;
}
