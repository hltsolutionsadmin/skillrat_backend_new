package com.skillrat.project.web.request;

import com.skillrat.project.domain.ProjectSLAType;
import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    private String code;
    private String description;
    
    private UUID b2bUnitId;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Valid
    private ClientInfo client;
    
    @NotNull(message = "Project type is required")
    private ProjectType projectType = ProjectType.INTERNAL;
    
    private ProjectSLAType status = ProjectSLAType.STANDARD;
    private ProjectStatus projectStatus = ProjectStatus.PLANNED;
    private boolean taskManagement = false;
    private boolean projectManagement = false;
    
    @Data
    public static class ClientInfo {
        private String name;
        private String primaryContactEmail;
        private String secondaryContactEmail;
    }
}
