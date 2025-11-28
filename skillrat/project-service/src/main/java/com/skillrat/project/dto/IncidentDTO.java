package com.skillrat.project.dto;

import com.skillrat.project.domain.IncidentImpact;
import com.skillrat.project.domain.IncidentPriority;
import com.skillrat.project.domain.IncidentStatus;
import com.skillrat.project.domain.IncidentUrgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {
    private UUID id;
    private String incidentNumber;
    private UUID projectId;
    private String projectCode;
    private String title;
    private String shortDescription;
    private IncidentUrgency urgency;
    private IncidentImpact impact;
    private IncidentPriority priority;
    private UUID categoryId;
    private String categoryName;
    private UUID subCategoryId;
    private String subCategoryName;
    private IncidentStatus status;
    private UUID assigneeId;
    private String assigneeName;
    private UUID reporterId;
    private String reporterName;
    private List<MediaDTO> media;
    private Instant createdDate;
    private Instant updatedDate;
    private String createdBy;
    private String updatedBy;
}
