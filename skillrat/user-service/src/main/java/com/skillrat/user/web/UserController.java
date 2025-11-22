package com.skillrat.user.web;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.user.domain.User;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.service.UserService;
import com.skillrat.user.service.OrganisationClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.skillrat.user.security.RequiresBusinessOrHrAdmin;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganisationClient organisationClient;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "mobile", u.getMobile(),
                        "firstName", u.getFirstName(),
                        "lastName", u.getLastName()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me/business")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> myBusiness(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .<ResponseEntity<?>>map(u -> {
                    java.util.UUID b2bUnitId = u.getB2bUnitId();
                    Object businessDetails = null;
                    if (b2bUnitId != null) {
                        businessDetails = organisationClient.getB2BUnit(b2bUnitId);
                    }
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", u.getId());
                    resp.put("email", u.getEmail());
                    resp.put("roles", (u.getRoles() == null
                            ? java.util.List.of()
                            : u.getRoles().stream()
                                .filter(r -> r != null)
                                .map(r -> r.getName())
                                .filter(n -> n != null)
                                .toList()));
                    if (b2bUnitId != null) resp.put("b2bUnitId", b2bUnitId);
                    if (businessDetails != null) resp.put("business", businessDetails);
                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignupRequest req) {
        User u = userService.signup(req.firstName, req.lastName, req.email, req.mobile, req.password);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userService.authenticate(req.emailOrMobile, req.password)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "mobile", u.getMobile(),
                        "roles", (u.getRoles() == null ? java.util.List.of() : u.getRoles().stream().map(r -> r.getName()).toList())
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }

    // Internal endpoint to assign existing user as business admin (unit-scoped)
    // Security: only the authenticated end-user can assign themselves (JWT sub must match email)
    @PostMapping("/internal/business-admin/assign")
    @PreAuthorize("#req.email != null && #req.email.equalsIgnoreCase(authentication.token.subject)")
    public ResponseEntity<User> assignBusinessAdmin(@RequestBody AssignBusinessAdminRequest req) {
        User u = userService.assignBusinessAdmin(req.b2bUnitId, req.email);
        return ResponseEntity.ok(u);
    }

    // Internal endpoint for cross-service user lookup by email
    @GetMapping("/internal/byEmail/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable("email") String email) {
        return userService.findByEmail(email)
                .map(u -> {
                    UUID b2bUnitId = u.getB2bUnitId();
                    Object businessDetails = null;

                    if (b2bUnitId != null) {
                        businessDetails = organisationClient.getB2BUnit(b2bUnitId);
                    }

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", u.getId());
                    resp.put("email", u.getEmail());

                    // Roles list (null-safe)
                    List<String> roles = (u.getRoles() == null)
                            ? List.of()
                            : u.getRoles().stream()
                            .filter(Objects::nonNull)
                            .map(r -> r.getName())
                            .filter(Objects::nonNull)
                            .toList();

                    resp.put("roles", roles);

                    if (b2bUnitId != null) {
                        resp.put("b2bUnitId", b2bUnitId);
                    }

                    if (businessDetails != null) {
                        resp.put("business", businessDetails);
                    }

                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Business admin invites an employee and assigns roles
    @PostMapping("/{b2bUnitId}/employees/invite")
    @RequiresBusinessOrHrAdmin
    public ResponseEntity<Employee> inviteEmployee(@PathVariable("b2bUnitId") UUID b2bUnitId,
                                                   @RequestBody InviteEmployeeRequest req) {
        Employee e = userService.inviteEmployee(b2bUnitId, req.firstName, req.lastName, req.email, req.mobile, req.roleIds);
        return ResponseEntity.ok(e);
    }

    // Employee sets password first time using setup token
    @PostMapping("/password/setup")
    public ResponseEntity<?> setupPassword(@RequestBody SetupPasswordRequest req) {
        boolean ok = userService.setupPassword(req.token, req.newPassword);
        return ok ? ResponseEntity.ok(Map.of("status", "ok")) : ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getUserById(@PathVariable("userId") UUID userId) throws Exception {
        return userService.getUserById(userId);
    }

    public static class SignupRequest {
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        @NotBlank public String password;
    }

    public static class LoginRequest {
        @NotBlank public String emailOrMobile;
        @NotBlank public String password;
    }

    public static class CreateBusinessAdminRequest {
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        public java.util.UUID b2bUnitId;
    }

    public static class AssignBusinessAdminRequest {
        @NotBlank @Email public String email;
        public java.util.UUID b2bUnitId;
    }

    public static class InviteEmployeeRequest {
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        @NotEmpty public java.util.List<java.util.UUID> roleIds;
    }

    public static class SetupPasswordRequest {
        @NotBlank public String token;
        @NotBlank public String newPassword;
    }
}
