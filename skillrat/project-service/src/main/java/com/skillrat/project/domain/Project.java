package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "project",
        indexes = {
                @Index(name = "idx_project_tenant", columnList = "tenant_id"),
                @Index(name = "idx_project_b2b_unit", columnList = "b2b_unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 64, unique = true)
    private String code;
    
    @Column
    private String description;

    @Column(nullable = false)
    private UUID b2bUnitId;

    // Optional holiday calendar selected for this project
    private UUID holidayCalendarId;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WBSElement> wbsElements = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id", unique = true)
    private ProjectClient client;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectSLAType status = ProjectSLAType.STANDARD;
}
