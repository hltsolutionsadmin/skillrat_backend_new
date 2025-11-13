package com.skillrat.user.web;

import com.skillrat.user.domain.Role;
import com.skillrat.user.service.RoleService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@Validated
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody CreateRoleRequest req) {
    	Role role = new Role();
    	role.setName(req.name);
    	role.setId(req.uid);
        Role r = Objects.nonNull(req.b2bUnitId) ? roleService.createRole(req.b2bUnitId, req.name) : roleService.createRole(role);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/{b2bUnitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> list(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        return roleService.list(b2bUnitId);
    }

    public static class CreateRoleRequest {
    	private UUID uid;
        public UUID b2bUnitId;
        @NotBlank public String name;
    }
}
