package com.skillrat.project.dto.response;

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
public class IncidentCommentResponse {
    private UUID id;
    private String comment;
    private UUID incidentId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userAvatarUrl;
    private List<String> attachmentUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
