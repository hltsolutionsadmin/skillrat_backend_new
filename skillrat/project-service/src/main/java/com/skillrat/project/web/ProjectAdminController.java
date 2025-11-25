package com.skillrat.project.web;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.project.domain.*;
import com.skillrat.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectAdminController {

    private final ProjectService service;

    public ProjectAdminController(ProjectService service) {
        this.service = service;
    }

    // Create Project - Only organization admin can create projects
    @PostMapping
    //@PreAuthorize("hasRole('BUSINESS_ADMIN')")
    public ResponseEntity<Project> createProject(@RequestBody @Valid CreateProjectRequest req) {
        String userId = getCurrentUserId();
        Project p = service.createProject(
                req.name,
                req.code,
                req.description,
                req.b2bUnitId,
                req.startDate,
                req.endDate,
                req.client != null ? req.client.name : null,
                req.client != null ? req.client.primaryContactEmail : null,
                req.client != null ? req.client.secondaryContactEmail : null,
                req.projectType,
                req.status,
                req.projectStatus,
                userId
        );
        return ResponseEntity.ok(p);
    }

    // Create WBS under a project
    @PostMapping("/{projectId}/wbs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WBSElement> createWbs(@PathVariable("projectId") UUID projectId,
                                                @RequestBody @Valid CreateWbsRequest req) {
        WBSElement w = service.createWbs(projectId, req.name, req.code, req.category, req.startDate, req.endDate);
        return ResponseEntity.ok(w);
    }

    // Update Project - Only organization admin can update projects
    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> updateProject(@PathVariable("projectId") UUID projectId,
                                                 @RequestBody @Valid UpdateProjectRequest req) {
        String userId = getCurrentUserId();
        Project p = service.updateProject(
                projectId,
                req.name,
                req.code,
                req.description,
                req.startDate,
                req.endDate,
                req.client != null ? req.client.name : null,
                req.client != null ? req.client.primaryContactEmail : null,
                req.client != null ? req.client.secondaryContactEmail : null,
                req.projectType,
                req.status,
                req.projectStatus,
                userId
        );
        return ResponseEntity.ok(p);
    }

    // Add or update project member (with project role and reporting manager)
    @PutMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectMember> upsertMember(@PathVariable("projectId") UUID projectId,
                                                      @RequestBody @Valid UpsertMemberRequest req) {
        ProjectMember m = service.addOrUpdateMember(projectId, req.employeeId, req.role, req.reportingManagerId,
                req.startDate, req.endDate, req.active != null ? req.active : true);
        return ResponseEntity.ok(m);
    }

    // Allocate member to a WBS (assignment that controls time entry eligibility)
    @PostMapping("/members/{projectId}/allocations/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WBSAllocation> allocate(@PathVariable("projectId") UUID projectId,
                                                  @PathVariable("userId") UUID userId,
                                                  @RequestBody @Valid AllocateRequest req) {
        WBSAllocation a = service.allocateMemberToWbs(projectId,userId, req.wbsId, req.startDate, req.endDate);
        return ResponseEntity.ok(a);
    }


    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable("projectId") UUID projectId) {
        Project p = service.getProject(projectId);
        return ResponseEntity.ok(p);
    }

    // Get WBS element by ID
    @GetMapping("/wbs/{wbsId}")
    public ResponseEntity<WBSElement> getWbs(@PathVariable("wbsId") UUID wbsId) {
        WBSElement w = service.getWbs(wbsId);
        return ResponseEntity.ok(w);
    }

    // List WBS elements for a project with pagination
    @GetMapping("/{projectId}/wbs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<WBSElement>> listWbs(
            @PathVariable("projectId") UUID projectId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<WBSElement> wbs = service.listWbs(projectId, pageRequest);
        return ResponseEntity.ok(wbs);
    }

    // List projects with pagination
    @GetMapping
    public ResponseEntity<Page<Project>> listProjects(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "status", required = false) ProjectStatus status
    ) {

        String email = getCurrentUserId();
        List<String> roles = getCurrentUserRoles();
        boolean isAdmin = roles.contains("BUSINESS_ADMIN");

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Project> projects;
        if (isAdmin) {
            projects = service.listProjectsForAdmin(pageRequest);
        } else {
            // For regular users, get only their assigned projects
            projects = service.listProjectsForUser(email, pageRequest);
        }

            List<Project> filtered = projects.getContent().stream()
                    .filter(p -> {
                        boolean statusMatch = (status == null) || (p.getProjectStatus() == status);
                        LocalDate s = p.getStartDate();
                        LocalDate e = p.getEndDate();
                        boolean afterFrom = (fromDate == null) || (e == null || !e.isBefore(fromDate));
                        boolean beforeTo = (toDate == null) || (s == null || !s.isAfter(toDate));
                        return statusMatch && afterFrom && beforeTo;
                    })
                    .collect(Collectors.toList());
            Page<Project> finalResult = new PageImpl<>(filtered, pageRequest, filtered.size());
            return ResponseEntity.ok(finalResult);
    }

    // Get basic user details for all members of a project
    @GetMapping("/{projectId}/members/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> listMemberUsers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(service.listMemberUsers(projectId));
    }
    @PutMapping("/wbs/{wbsId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WBSElement> updateWbs(@PathVariable UUID wbsId, @RequestBody @Valid UpdateWbsRequest req) {
        WBSElement w = service.updateWbs(wbsId, req.name, req.code, req.category, req.startDate, req.endDate,req.projectId,req.disabled);
        return ResponseEntity.ok(w);
    }

    @DeleteMapping("/{projectId}/removeMember/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectId,
                                             @PathVariable UUID employeeId) {
        service.removeMember(projectId, employeeId);
        return ResponseEntity.noContent().build();
    }

    public static class UpdateWbsRequest {
        public String name;
        public String code;
        public UUID projectId;
        public WBSCategory category;
        public LocalDate startDate;
        public LocalDate endDate;
        public boolean disabled;
    }
    // Helper methods to get current user info
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getName();
        }
        throw new SecurityException("User not authenticated");
    }
    
    private List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replace("ROLE_", ""))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public static class CreateProjectRequest {
        @NotBlank public String name;
        public String code;
        @NotNull public String b2bUnitId;
        public LocalDate startDate;
        public LocalDate endDate;
        public String description;
        public ProjectClientRequest client;
        public ProjectType projectType;
        public ProjectSLAType status;
        public ProjectStatus projectStatus;
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

    public static class UpdateProjectRequest {
        public String name;
        public String code;
        public String description;
        public LocalDate startDate;
        public LocalDate endDate;
        public ProjectClientRequest client;
        public ProjectType projectType;
        public ProjectSLAType status;
        public ProjectStatus projectStatus;
    }

}
