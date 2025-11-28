package com.skillrat.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID b2bUnitId;
    private UUID holidayCalendarId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectType projectType;
    private ProjectClientDTO client;
    private List<WBSElementDTO> wbsElements;
    private List<ProjectMemberDTO> members;
    private ProjectSLAType status;
    private ProjectStatus projectStatus;
    private boolean taskManagement;
    private boolean projectManagement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
