package com.skillrat.project.dto.request;

import com.skillrat.project.domain.IncidentImpact;
import com.skillrat.project.domain.IncidentUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class IncidentCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Short description is required")
    private String shortDescription;

    @NotNull(message = "Urgency is required")
    private IncidentUrgency urgency;

    @NotNull(message = "Impact is required")
    private IncidentImpact impact;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private UUID subCategoryId;

    private List<MultipartFile> mediaFiles = new ArrayList<>();

    private List<String> mediaUrls = new ArrayList<>();

    private UUID assigneeId;

    private UUID reporterId;

    public List<MultipartFile> getMediaFiles() {
        return mediaFiles != null ? mediaFiles : new ArrayList<>();
    }

    public List<String> getMediaUrls() {
        return mediaUrls != null ? mediaUrls : new ArrayList<>();
    }
}
