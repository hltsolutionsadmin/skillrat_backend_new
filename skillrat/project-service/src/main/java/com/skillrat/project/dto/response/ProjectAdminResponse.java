package com.skillrat.project.dto.response;

import com.skillrat.project.domain.ProjectStatus;
import com.skillrat.project.domain.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAdminResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID b2bUnitId;
    private String b2bUnitName;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectType projectType;
    private ProjectStatus projectStatus;
    private boolean taskManagement;
    private boolean projectManagement;
    private ClientInfo client;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private String name;
        private String primaryContactEmail;
        private String secondaryContactEmail;
    }
}
