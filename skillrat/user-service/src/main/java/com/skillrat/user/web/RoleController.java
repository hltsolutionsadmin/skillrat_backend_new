package com.skillrat.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.user.domain.Role;
import com.skillrat.user.security.RequiresBusinessOrHrAdmin;
import com.skillrat.user.service.BusinessRoleService;
import com.skillrat.user.service.ProjectRoleService;
import com.skillrat.user.service.RoleService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * REST controller for managing roles and role assignments.
 */
@RestController
@RequestMapping("/api/roles")
@Validated
public class RoleController {

    private final RoleService roleService;
    private final BusinessRoleService businessRoleService;
    private final ProjectRoleService projectRoleService;

    public RoleController(RoleService roleService, 
                         BusinessRoleService businessRoleService,
                         ProjectRoleService projectRoleService) {
        this.roleService = roleService;
        this.businessRoleService = businessRoleService;
        this.projectRoleService = projectRoleService;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setB2bUnitId(request.getB2bUnitId());
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> getRoleById(@PathVariable @NonNull UUID roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> updateRole(
            @PathVariable @NonNull UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        
        Role updatedRole = roleService.updateRole(roleId, request.getName(), request.getDescription());
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteRole(@PathVariable @NonNull UUID roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/system/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Role>> listSystemRoles(@PathVariable UUID b2bUnitId) {
        return ResponseEntity.ok(roleService.getSystemRoles(b2bUnitId));
    }

    @GetMapping("/business/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Role>> listBusinessRoles(@PathVariable UUID b2bUnitId) {
        return ResponseEntity.ok(roleService.getRolesByBusiness(b2bUnitId));
    }

    // ========== Business Role Assignments ==========

    @SuppressWarnings("null")
	@PostMapping("/business/assign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> assignBusinessRole(
            @Valid @RequestBody AssignBusinessRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String assignedBy = jwt.getSubject();
        businessRoleService.assignRoleToUser(
            request.getUserId(), 
            request.getBusinessId(), 
            request.getRoleName(),
            assignedBy
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @SuppressWarnings("null")
	@PostMapping("/business/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBusinessRole(@Valid @RequestBody RemoveBusinessRoleRequest request) {
        businessRoleService.removeRoleFromUser(
            request.getUserId(),
            request.getBusinessId(),
            request.getRoleName()
        );
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/business/{businessId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BusinessRoleAssignmentResponse>> getBusinessRoleAssignments(
            @PathVariable UUID businessId) {
        
        // This would require a service method to convert entities to DTOs
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ========== Project Role Assignments ==========

    @SuppressWarnings("null")
	@PostMapping("/project/assign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> assignProjectRole(
            @Valid @RequestBody AssignProjectRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String assignedBy = jwt.getSubject();
        projectRoleService.assignRoleToUser(
            request.getUserId(),
            request.getProjectId(),
            request.getRoleName(),
            assignedBy
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @SuppressWarnings("null")
	@PostMapping("/project/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeProjectRole(@Valid @RequestBody RemoveProjectRoleRequest request) {
        projectRoleService.removeRoleFromUser(
            request.getUserId(),
            request.getProjectId(),
            request.getRoleName()
        );
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectRoleAssignmentResponse>> getProjectRoleAssignments(
            @PathVariable UUID projectId) {
        
        // This would require a service method to convert entities to DTOs
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ========== Request/Response DTOs ==========

    public static class CreateRoleRequest {
        @NotBlank
        private String name;
        
        private String description;
        
        private UUID b2bUnitId;
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public UUID getB2bUnitId() {
            return b2bUnitId;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public void setB2bUnitId(UUID b2bUnitId) {
            this.b2bUnitId = b2bUnitId;
        }
    }

    public static class UpdateRoleRequest {
        @NotBlank
        private String name;
        
        private String description;
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }

    @RequiresBusinessOrHrAdmin
    public static class AssignBusinessRoleRequest {
        @NotNull
        private UUID userId;
        @NotNull
        private UUID businessId;
        @NotBlank
        private String roleName;
        
        public UUID getUserId() {
            return userId;
        }
        
        public UUID getBusinessId() {
            return businessId;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        public void setUserId(UUID userId) {
            this.userId = userId;
        }
        
        public void setBusinessId(UUID businessId) {
            this.businessId = businessId;
        }
        
        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }

    @RequiresBusinessOrHrAdmin
    public static class RemoveBusinessRoleRequest {
        @NotNull
        private UUID userId;
        @NotNull
        private UUID businessId;
        @NotBlank
        private String roleName;
        
        public UUID getUserId() {
            return userId;
        }
        
        public UUID getBusinessId() {
            return businessId;
        }
        
        public String getRoleName() {
            return roleName;
        }
    }

    public static class AssignProjectRoleRequest {
        @NotNull
        private UUID userId;
        @NotNull
        private UUID projectId;
        @NotBlank
        private String roleName;
        
        public UUID getUserId() {
            return userId;
        }
        
        public UUID getProjectId() {
            return projectId;
        }
        
        public String getRoleName() {
            return roleName;
        }
    }

    public static class RemoveProjectRoleRequest {
        @NotNull
        private UUID userId;
        @NotNull
        private UUID projectId;
        @NotBlank
        private String roleName;
        
        public UUID getUserId() {
            return userId;
        }
        
        public UUID getProjectId() {
            return projectId;
        }
        
        public String getRoleName() {
            return roleName;
        }
    }

    // ... existing code ...

    // ========== Response DTOs ==========

    public static class BusinessRoleAssignmentResponse {
        private UUID userId;
        private UUID businessId;
        private String roleName;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getBusinessId() {
            return businessId;
        }

        public void setBusinessId(UUID businessId) {
            this.businessId = businessId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }

    public static class ProjectRoleAssignmentResponse {
        private UUID userId;
        private UUID projectId;
        private String roleName;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getProjectId() {
            return projectId;
        }

        public void setProjectId(UUID projectId) {
            this.projectId = projectId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }
}
