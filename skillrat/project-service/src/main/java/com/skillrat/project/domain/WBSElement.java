package com.skillrat.project.domain;

import java.time.LocalDate;

import com.skillrat.common.orm.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
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
}
