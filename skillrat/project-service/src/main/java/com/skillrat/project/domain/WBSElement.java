package com.skillrat.project.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "wbs_element",
        indexes = {
                @Index(name = "idx_wbs_project", columnList = "project_id"),
                @Index(name = "idx_wbs_tenant", columnList = "tenant_id"),
                @Index(name = "idx_wbs_code_tenant", columnList = "code, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class WBSElement extends BaseEntity {

    @ManyToOne(optional = true)
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 64, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WBSCategory category = WBSCategory.OTHER;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @Column(nullable = false)
    private UUID b2bUnitId;
}
