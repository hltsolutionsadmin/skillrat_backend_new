package com.skillrat.project.dto.request;


import com.skillrat.project.domain.ProjectClientDTO;
import com.skillrat.project.domain.ProjectSLAType;
import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import com.skillrat.project.web.ProjectAdminController;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a new project.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateProjectRequest {
    @NotBlank public String name;
    public String code;
    @NotNull public String b2bUnitId;
    public LocalDate startDate;
    public LocalDate endDate;
    public String description;
    public com.skillrat.project.web.request.CreateProjectRequest.ClientInfo client;
    public ProjectType projectType;
    public ProjectSLAType status;
    public ProjectStatus projectStatus;
    public boolean taskManagement;
    public boolean projectManagement;
    private ProjectSLAType slaType;


}