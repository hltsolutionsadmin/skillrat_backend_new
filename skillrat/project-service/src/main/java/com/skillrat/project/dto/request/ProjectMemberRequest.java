package com.skillrat.project.dto.request;

import com.skillrat.project.domain.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectMemberRequest {
    private UUID id;
    
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private ProjectRole role = ProjectRole.DEVELOPER;
    private UUID reportingManagerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;
}
