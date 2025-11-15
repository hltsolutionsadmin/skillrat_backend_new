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
@Table(name = "project_member")
@Getter
@Setter
@NoArgsConstructor
public class ProjectMember extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @Column(nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectRole role = ProjectRole.DEVELOPER;

    // Reporting manager for this project context
    private UUID reportingManagerId;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active = true;
}
