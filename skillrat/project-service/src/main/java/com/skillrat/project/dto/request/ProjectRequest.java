package com.skillrat.project.dto.request;

import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import com.skillrat.project.domain.ProjectSLAType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectRequest {
    private UUID id;
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String code;
    private String description;
    
    @NotNull(message = "B2B Unit ID is required")
    private UUID b2bUnitId;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String clientName;
    private String clientPrimaryEmail;
    private String clientSecondaryEmail;
    
    @NotNull(message = "Project type is required")
    private ProjectType projectType;
    
    private ProjectSLAType status = ProjectSLAType.STANDARD;
    private ProjectStatus projectStatus = ProjectStatus.PLANNED;
    private boolean taskManagement = false;
    private boolean projectManagement = false;
}
