package com.skillrat.project.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@JsonIgnoreProperties({"incidents"})
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

    private UUID holidayCalendarId;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 32)
    private ProjectType projectType = ProjectType.INTERNAL;

    @JsonBackReference
    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<WBSElement> wbsElements = new ArrayList<>();


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProjectMember> members = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id", unique = true)
    private ProjectClient client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectSLAType status = ProjectSLAType.STANDARD;


    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false, length = 32)
    private ProjectStatus projectStatus = ProjectStatus.PLANNED;

    @Column(name = "task_management_enabled", nullable = false)
    private boolean taskManagement = false;

    @Column(name = "incident_management_enabled", nullable = false)
    private boolean projectManagement = false;
}

