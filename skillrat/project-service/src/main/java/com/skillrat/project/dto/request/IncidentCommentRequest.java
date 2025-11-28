package com.skillrat.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class IncidentCommentRequest {
    @NotBlank(message = "Comment text is required")
    private String comment;
    
    @NotNull(message = "Incident ID is required")
    private String incidentId;
    
    private List<MultipartFile> attachments;
    private List<String> attachmentUrls;
}
