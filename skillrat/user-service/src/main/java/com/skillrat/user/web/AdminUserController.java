package com.skillrat.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.user.domain.User;
import com.skillrat.user.security.B2BUnitAccessValidator;
import com.skillrat.user.service.UserService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/admin/users")
@Validated

public class AdminUserController {

    private final UserService userService;
    private final B2BUnitAccessValidator b2bUnitAccessValidator;


    public AdminUserController(UserService userService, B2BUnitAccessValidator b2bUnitAccessValidator) { this.userService = userService;
        this.b2bUnitAccessValidator = b2bUnitAccessValidator;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<User> search(@RequestParam("b2bUnitId") UUID b2bUnitId,
                                 @RequestParam(value = "q", required = false) String q,
                                 Pageable pageable) {
        b2bUnitAccessValidator.validateCurrentUserBelongsTo(b2bUnitId);
        return userService.searchUsers(b2bUnitId,q, null, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> get(@PathVariable("id") @NonNull UUID id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
        User u = userService.adminCreateUser(req.b2bUnitId, req.firstName, req.lastName, req.email, req.mobile, req.roleIds);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
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
