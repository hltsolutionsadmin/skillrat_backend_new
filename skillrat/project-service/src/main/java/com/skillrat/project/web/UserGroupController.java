package com.skillrat.project.web;

import com.skillrat.project.domain.UserGroup;
import com.skillrat.project.domain.UserGroupMember;
import com.skillrat.project.domain.UserGroupRole;
import com.skillrat.project.service.UserGroupService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-groups")
@Validated
public class UserGroupController {

    private final UserGroupService service;

    public UserGroupController(UserGroupService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO','PROJECT_MANAGER','TEAM_LEAD')")
    public ResponseEntity<UserGroup> create(@RequestBody CreateGroupRequest req) {
        UserGroup g = service.create(req.name, req.description, req.b2bUnitId, req.projectId, req.leadId);
        return ResponseEntity.ok(g);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserGroup> get(@PathVariable("id") UUID id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/byProject/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public List<UserGroup> byProject(@PathVariable("projectId") UUID projectId) {
        return service.listByProject(projectId);
    }

    @GetMapping("/byB2BUnit/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public List<UserGroup> byB2BUnit(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        return service.listByB2BUnit(b2bUnitId);
    }

    @GetMapping("/byLead/{leadId}")
    @PreAuthorize("isAuthenticated()")
    public List<UserGroup> byLead(@PathVariable("leadId") UUID leadId) {
        return service.listByLead(leadId);
    }

    @GetMapping("/byUser/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<UserGroup> byUser(@PathVariable("userId") UUID userId) {
        return service.listByUser(userId);
    }

    @PostMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO','PROJECT_MANAGER','TEAM_LEAD')")
    public ResponseEntity<UserGroupMember> addMember(@PathVariable("groupId") UUID groupId, @RequestBody AddMemberRequest req) {
        return ResponseEntity.ok(service.addMember(groupId, req.userId, req.role));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','PMO','PROJECT_MANAGER','TEAM_LEAD')")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable("groupId") UUID groupId, @PathVariable("userId") UUID userId) {
        service.removeMember(groupId, userId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("isAuthenticated()")
    public List<UserGroupMember> listMembers(@PathVariable("groupId") UUID groupId) {
        return service.listMembers(groupId);
    }

    public static class CreateGroupRequest {
        @NotBlank public String name;
        public String description;
        @NotNull public UUID b2bUnitId;
        public UUID projectId; // optional for org-level
        public UUID leadId; // optional
    }

    public static class AddMemberRequest {
        @NotNull public UUID userId;
        public UserGroupRole role; // optional, default MEMBER
    }
}
