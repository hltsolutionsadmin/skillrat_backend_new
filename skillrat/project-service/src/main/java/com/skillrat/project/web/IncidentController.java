package com.skillrat.project.web;

import com.skillrat.project.domain.*;
import com.skillrat.project.service.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping(value = "/projects/{projectId}/incidents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Incident> create(
            @PathVariable("projectId") UUID projectId,
            @Valid @ModelAttribute CreateIncidentRequest request) throws Exception {


        try {
            Incident incident = incidentService.create(
                    projectId,
                    request.getTitle(),
                    request.getShortDescription(),
                    request.getUrgency(),
                    request.getImpact(),
                    request.getCategoryId(),
                    request.getSubCategoryId(),
                    request.getMediaFiles(),
                    request.getMediaUrls(),
                    request.getAssigneeId(),
                    request.getReporterId()
            );
            return ResponseEntity.ok(incident);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/projects/{projectId}/incidents")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByProject(@PathVariable("projectId") UUID projectId,
                                        @RequestParam(defaultValue = "0") @Min(0) int page,
                                        @RequestParam(defaultValue = "20") @Min(1) int size,
                                        @RequestParam(value = "priority", required = false) IncidentPriority priority,
                                        @RequestParam(value = "categoryId", required = false) UUID categoryId,
                                        @RequestParam(value = "status", required = false) IncidentStatus status,
                                        @RequestParam(value = "search", required = false) String search) {
        boolean hasFilters = priority != null || categoryId != null || status != null || (search != null && !search.isBlank());
        if (hasFilters) {
            return incidentService.listByProjectFiltered(projectId, priority, categoryId, status, search, PageRequest.of(page, size));
        }
        return incidentService.listByProject(projectId, PageRequest.of(page, size));
    }

    @PutMapping("/incidents/{incidentId}/assignee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> assignAssignee(@PathVariable("incidentId") UUID incidentId,
                                                   @RequestBody @Valid AssignUserRequest req) throws Exception {
        Incident updated = incidentService.assignAssignee(incidentId, req.userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/incidents/{incidentId}/reporter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> assignReporter(@PathVariable("incidentId") UUID incidentId,
                                                   @RequestBody @Valid AssignUserRequest req) throws Exception {
        Incident updated = incidentService.assignReporter(incidentId, req.userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/incidents/{incidentId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> updateStatus(@PathVariable("incidentId") UUID incidentId,
                                                 @Valid @ModelAttribute UpdateStatusRequest request) {
        Incident updated = incidentService.updateStatus(incidentId, request.status,
                request.getShortDescription(),
                request.getUrgency(),
                request.getImpact(),
                request.getMediaFiles(),
                request.getMediaUrls());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/incidents/{incidentId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<Incident>> history(@PathVariable("incidentId") UUID incidentId) {
        return ResponseEntity.ok(incidentService.history(incidentId));
    }

    @GetMapping("/incidents/{incidentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> getById(@PathVariable("incidentId") UUID incidentId) {
        return ResponseEntity.ok(incidentService.getById(incidentId));
    }

    @GetMapping("/projects/{projectId}/assignee")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByAssignee(@PathVariable("projectId") UUID projectId,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) int size) {
        return incidentService.listByProjectAndLoggedInUser(projectId, PageRequest.of(page, size));
    }

    @GetMapping("/incidents/reporter/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByReporter(@PathVariable("projectId") UUID projectId,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) int size) {
        return incidentService.listByReporter(projectId, PageRequest.of(page, size));
    }
    @Data
    public static class CreateIncidentRequest {
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

        // Getters and setters
        public List<MultipartFile> getMediaFiles() {
            return mediaFiles != null ? mediaFiles : new ArrayList<>();
        }

        public List<String> getMediaUrls() {
            return mediaUrls != null ? mediaUrls : new ArrayList<>();
        }
    }

    public static class AssignUserRequest {
        @NotNull public UUID userId;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull public IncidentStatus status;
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
    }
}
