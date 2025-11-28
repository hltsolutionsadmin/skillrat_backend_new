package com.skillrat.project.dto.request;

import com.skillrat.project.domain.IncidentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class IncidentRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Urgency is required")
    private IncidentPriority urgency;
    
    @NotBlank(message = "Impact is required")
    private String impact;
    
    private UUID categoryId;
    private UUID subCategoryId;
    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private UUID assigneeId;
    private UUID reporterId;
}
