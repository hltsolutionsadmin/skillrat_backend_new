package com.skillrat.project.dto;

import com.skillrat.project.domain.ProjectRole;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProjectMemberResponseDTO {
    private UUID id;
    private UUID employeeId;
    private UUID projectId;
    private ProjectRole role;
    private UUID reportingManagerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
