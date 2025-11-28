package com.skillrat.project.web;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.project.domain.*;
import com.skillrat.project.service.ProjectService;
import com.skillrat.project.web.request.*;
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
    public ResponseEntity<ProjectDTO> createProject(@RequestBody @Valid CreateProjectRequest req) {
        String userId = getCurrentUserId();
        ProjectDTO dto = service.createProject(req, userId);
        return ResponseEntity.ok(dto);
    }

    // Create WBS under a project
    @PostMapping("/{projectId}/wbs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WBSElementDTO> createWbs(@PathVariable("projectId") UUID projectId,
                                                   @RequestBody @Valid CreateWbsRequest req) {
        WBSElement w = service.createWbs(projectId, req);
        return ResponseEntity.ok(service.toWbsDto(w));
    }

    // Update Project - Only organization admin can update projects
    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable("projectId") UUID projectId,
                                                    @RequestBody @Valid UpdateProjectRequest req) {
        String userId = getCurrentUserId();
        ProjectDTO dto = service.updateProject(projectId, req, userId);
        return ResponseEntity.ok(dto);
    }

    // Add or update project member (with project role and reporting manager)
    @PutMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectMemberDTO> upsertMember(@PathVariable("projectId") UUID projectId,
                                                         @RequestBody @Valid UpsertMemberRequest req) {
        ProjectMember m = service.addOrUpdateMember(projectId, req);
        return ResponseEntity.ok(service.toMemberDto(m));
    }

    // Allocate member to a WBS (assignment that controls time entry eligibility)
    @PostMapping("/members/{projectId}/allocations/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WBSAllocationDTO> allocate(@PathVariable("projectId") UUID projectId,
                                                     @PathVariable("userId") UUID userId,
                                                     @RequestBody @Valid AllocateRequest req) {
        WBSAllocation a = service.allocateMemberToWbs(projectId, userId, req);
        return ResponseEntity.ok(service.toAllocationDto(a));
    }


    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable("projectId") UUID projectId) {
        ProjectDTO p = service.getProjectDTO(projectId);
        return ResponseEntity.ok(p);
    }

    // Get WBS element by ID
    @GetMapping("/wbs/{wbsId}")
    public ResponseEntity<WBSElementDTO> getWbs(@PathVariable("wbsId") UUID wbsId) {
        WBSElement w = service.getWbs(wbsId);
        return ResponseEntity.ok(service.toWbsDto(w));
    }

    // List WBS elements for a project with pagination
    @GetMapping("/{projectId}/wbs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<WBSElementDTO>> listWbs(
            @PathVariable("projectId") UUID projectId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<WBSElement> wbs = service.listWbs(projectId, pageRequest);
        List<WBSElementDTO> mapped = wbs.getContent().stream().map(service::toWbsDto).collect(Collectors.toList());
        Page<WBSElementDTO> dtoPage = new PageImpl<>(mapped, pageRequest, wbs.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    // List projects with pagination
    @GetMapping
    public ResponseEntity<Page<ProjectDTO>> listProjects(
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
            List<ProjectDTO> mapped = filtered.stream().map(service::toDto).collect(Collectors.toList());
            Page<ProjectDTO> finalResult = new PageImpl<>(mapped, pageRequest, mapped.size());
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
    public ResponseEntity<WBSElementDTO> updateWbs(@PathVariable UUID wbsId, @RequestBody @Valid UpdateWbsRequest req) {
        WBSElement w = service.updateWbs(wbsId, req);
        return ResponseEntity.ok(service.toWbsDto(w));
    }

    @DeleteMapping("/{projectId}/removeMember/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectId,
                                             @PathVariable UUID employeeId) {
        service.removeMember(projectId, employeeId);
        return ResponseEntity.noContent().build();
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


}
