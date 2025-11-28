package com.skillrat.project.dto;

import com.skillrat.project.domain.ProjectClientDTO;
import com.skillrat.project.domain.ProjectSLAType;
import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
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
    private ProjectSLAType status;
    private ProjectStatus projectStatus;
    private boolean taskManagement;
    private boolean projectManagement;
    private String createdBy;
    private String lastModifiedBy;
}
