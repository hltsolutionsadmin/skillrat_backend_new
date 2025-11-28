package com.skillrat.project.dto.response;

import com.skillrat.project.domain.IncidentPriority;
import com.skillrat.project.domain.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    private UUID id;
    private String title;
    private String description;
    private IncidentPriority urgency;
    private String impact;
    private UUID projectId;
    private String projectName;
    private UUID categoryId;
    private String categoryName;
    private UUID subCategoryId;
    private String subCategoryName;
    private List<String> mediaUrls;
    private UUID assigneeId;
    private String assigneeName;
    private UUID reporterId;
    private String reporterName;
    private IncidentStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
