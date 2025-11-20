package com.skillrat.user.web;

import com.skillrat.user.api.ApiResponse;
import com.skillrat.user.domain.Role;
import com.skillrat.user.dto.CreateRoleRequest;
import com.skillrat.user.dto.RoleDto;
import com.skillrat.user.populator.RolePopulator;
import com.skillrat.user.security.RequiresBusinessOrHrAdmin;
import com.skillrat.user.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@Validated
public class RoleController {
    private final RoleService roleService;
    private final RolePopulator rolePopulator;
    public RoleController(RoleService roleService, RolePopulator rolePopulator) {
        this.roleService = roleService;
        this.rolePopulator = rolePopulator;
    }

    @PostMapping
    @RequiresBusinessOrHrAdmin
    public ResponseEntity<ApiResponse<RoleDto>> create(@RequestBody CreateRoleRequest req) {
    	Role role = new Role();
    	role.setName(req.getName());
    	role.setId(req.getUid());
        Role r = Objects.nonNull(req.getB2bUnitId()) ? roleService.createRole(req.getB2bUnitId(), req.getName()) : roleService.createRole(role);
        RoleDto dto = rolePopulator.toDto(r);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RoleDto>>> list(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        List<RoleDto> items = roleService.list(b2bUnitId).stream().map(rolePopulator::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoleDto>>> listAll() {
        List<RoleDto> items = roleService.listAll().stream().map(rolePopulator::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }
}
