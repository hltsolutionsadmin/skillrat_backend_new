package com.skillrat.user.web;

import com.skillrat.user.domain.User;
import com.skillrat.user.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@Validated
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) { this.userService = userService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Page<User> search(@RequestParam(value = "q", required = false) String q,
                             Pageable pageable) {
        return userService.searchUsers(q, null, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN',BUSINESS_ADMIN,'HR')")
    public ResponseEntity<User> get(@PathVariable("id") UUID id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN','HR')")
    public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
        User u = userService.adminCreateUser(req.b2bUnitId, req.firstName, req.lastName, req.email, req.mobile, req.roleIds);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN','HR')")
    public ResponseEntity<User> update(@PathVariable("id") UUID id, @RequestBody UpdateUserRequest req) {
        User u = userService.adminUpdateUser(id, req.firstName, req.lastName, req.mobile, req.active, req.roleIds);
        return ResponseEntity.ok(u);
    }

    public static class CreateUserRequest {
        public UUID b2bUnitId;
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        @NotEmpty public List<UUID> roleIds;
    }

    public static class UpdateUserRequest {
        public String firstName;
        public String lastName;
        public String mobile;
        public Boolean active;
        public List<UUID> roleIds;
    }
}
