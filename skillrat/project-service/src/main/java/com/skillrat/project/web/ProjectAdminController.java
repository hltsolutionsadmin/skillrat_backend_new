package com.skillrat.project.web;

import com.skillrat.project.domain.*;
import com.skillrat.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectAdminController {

    private final ProjectService service;

    public ProjectAdminController(ProjectService service) {
        this.service = service;
    }

    // Get Project details by ID
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO')")
    public ResponseEntity<Project> getProject(@PathVariable("projectId") UUID projectId) {
        Project p = service.getProject(projectId);
        return ResponseEntity.ok(p);
    }

    // List Projects by Business (b2bUnitId)
    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO')")
    public Page<Project> listProjects(@RequestParam("b2bUnitId") UUID b2bUnitId, Pageable pageable) {
        return service.listByBusiness(b2bUnitId, pageable);
    }

    // Create Project
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO')")
    public ResponseEntity<Project> createProject(@RequestBody @Valid CreateProjectRequest req) {
        Project p = service.createProject(
                req.name,
                req.code,
                req.description,
                req.b2bUnitId,
                req.startDate,
                req.endDate,
                req.client != null ? req.client.name : null,
                req.client != null ? req.client.primaryContactEmail : null,
                req.client != null ? req.client.secondaryContactEmail : null
        );
        return ResponseEntity.ok(p);
    }

    // Create WBS under a project
    @PostMapping("/{projectId}/wbs")
    @PreAuthorize("hasAnyRole('ADMIN','PMO')")
    public ResponseEntity<WBSElement> createWbs(@PathVariable("projectId") UUID projectId,
                                                @RequestBody @Valid CreateWbsRequest req) {
        WBSElement w = service.createWbs(projectId, req.name, req.code, req.category, req.startDate, req.endDate);
        return ResponseEntity.ok(w);
    }

    // Add or update project member (with project role and reporting manager)
    @PutMapping("/{projectId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','PMO')")
    public ResponseEntity<ProjectMember> upsertMember(@PathVariable("projectId") UUID projectId,
                                                      @RequestBody @Valid UpsertMemberRequest req) {
        ProjectMember m = service.addOrUpdateMember(projectId, req.employeeId, req.role, req.reportingManagerId,
                req.startDate, req.endDate, req.active != null ? req.active : true);
        return ResponseEntity.ok(m);
    }

    // Allocate member to a WBS (assignment that controls time entry eligibility)
        @PostMapping("/members/{memberId}/allocations")
    @PreAuthorize("hasAnyRole('ADMIN','PMO')")
    public ResponseEntity<WBSAllocation> allocate(@PathVariable("memberId") UUID memberId,
                                                  @RequestBody @Valid AllocateRequest req) {
        WBSAllocation a = service.allocateMemberToWbs(memberId, req.wbsId, req.startDate, req.endDate);
        return ResponseEntity.ok(a);
    }


    public static class CreateProjectRequest {
        @NotBlank public String name;
        public String code;
        @NotNull public UUID b2bUnitId;
        public LocalDate startDate;
        public LocalDate endDate;
        public String description;
        public ProjectClientRequest client;
    }

    public static class CreateWbsRequest {
        @NotBlank public String name;
        public String code;
        public WBSCategory category;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    public static class UpsertMemberRequest {
        @NotNull public UUID employeeId;
        public ProjectRole role;
        public UUID reportingManagerId;
        public LocalDate startDate;
        public LocalDate endDate;
        public Boolean active;
    }

    public static class AllocateRequest {
        @NotNull public UUID wbsId;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    public static class ProjectClientRequest {
        @NotBlank public String name;
        public String primaryContactEmail;
        public String secondaryContactEmail;
    }

}
