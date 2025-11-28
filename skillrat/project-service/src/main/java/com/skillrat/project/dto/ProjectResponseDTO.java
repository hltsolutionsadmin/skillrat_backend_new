package com.skillrat.project.dto;

import com.skillrat.project.domain.ProjectClientDTO;
import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import com.skillrat.project.domain.ProjectSLAType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID b2bUnitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectClientDTO client;
    private ProjectType projectType;
    private ProjectSLAType status;
    private ProjectStatus projectStatus;
    private boolean taskManagement;
    private boolean projectManagement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
