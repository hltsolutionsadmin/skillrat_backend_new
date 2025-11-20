package com.skillrat.project.web;

import com.skillrat.project.domain.*;
import com.skillrat.project.service.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/projects/{projectId}/incidents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> create(@PathVariable("projectId") UUID projectId,
                                           @RequestBody @Valid CreateIncidentRequest req) {
        Incident incident = incidentService.create(
                projectId,
                req.title,
                req.shortDescription,
                req.urgency,
                req.impact,
                req.category,
                req.subCategory
        );
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/projects/{projectId}/incidents")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByProject(@PathVariable("projectId") UUID projectId, Pageable pageable) {
        return incidentService.listByProject(projectId, pageable);
    }

    @PutMapping("/incidents/{incidentId}/assignee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> assignAssignee(@PathVariable("incidentId") UUID incidentId,
                                                   @RequestBody @Valid AssignUserRequest req) {
        Incident updated = incidentService.assignAssignee(incidentId, req.userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/incidents/{incidentId}/reporter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> assignReporter(@PathVariable("incidentId") UUID incidentId,
                                                   @RequestBody @Valid AssignUserRequest req) {
        Incident updated = incidentService.assignReporter(incidentId, req.userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/incidents/{incidentId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Incident> updateStatus(@PathVariable("incidentId") UUID incidentId,
                                                 @RequestBody @Valid UpdateStatusRequest req) {
        Incident updated = incidentService.updateStatus(incidentId, req.status);
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

    @GetMapping("/incidents/assignee/{assigneeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByAssignee(@PathVariable("assigneeId") UUID assigneeId, Pageable pageable) {
        return incidentService.listByAssignee(assigneeId, pageable);
    }

    @GetMapping("/incidents/reporter/{reporterId}")
    @PreAuthorize("isAuthenticated()")
    public Page<Incident> listByReporter(@PathVariable("reporterId") UUID reporterId, Pageable pageable) {
        return incidentService.listByReporter(reporterId, pageable);
    }
    public static class CreateIncidentRequest {
        @NotBlank public String title;
        @NotBlank public String shortDescription;
        @NotNull public IncidentUrgency urgency;
        @NotNull public IncidentImpact impact;
        public IncidentCategory category;
        public String subCategory;
    }

    public static class AssignUserRequest {
        @NotNull public UUID userId;
    }

    public static class UpdateStatusRequest {
        @NotNull public IncidentStatus status;
    }

    @GetMapping("/incidents/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncidentCategory>> listCategories() {
        return ResponseEntity.ok(Arrays.asList(IncidentCategory.values()));
    }

    @GetMapping("/incidents/impacts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncidentImpact>> listImpacts() {
        return ResponseEntity.ok(Arrays.asList(IncidentImpact.values()));
    }

    @GetMapping("/incidents/urgencies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncidentUrgency>> listUrgencies() {
        return ResponseEntity.ok(Arrays.asList(IncidentUrgency.values()));
    }
}
