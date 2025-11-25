package com.skillrat.user.web;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.user.domain.User;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.service.OtpService;
import com.skillrat.user.service.UserService;
import com.skillrat.user.service.OrganisationClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.skillrat.user.security.RequiresBusinessOrHrAdmin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
// Manual constructor replaces @RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganisationClient organisationClient;
    private final OtpService otpService;

    public UserController(UserService userService, OrganisationClient organisationClient, OtpService otpService) {
        this.userService = userService;
        this.organisationClient = organisationClient;
        this.otpService = otpService;
    }

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

    // Bulk lookup: given a list of user IDs, return basic user details (for project-service member mapping)
    @PostMapping("/internal/byIds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestBody IdsRequest req) {
        List<UserDTO> users = userService.getUsersByIds(req.ids);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getUserById(@PathVariable("userId") UUID userId) throws Exception {
        return userService.getUserById(userId);
    }

//    public static class SignupRequest {
//        @NotBlank public String firstName;
//        @NotBlank public String lastName;
//        @NotBlank @Email public String email;
//        public String mobile;
//        @NotBlank public String password;
//    }

    public static class LoginRequest {
        @NotBlank public String emailOrMobile;
        @NotBlank public String password;
    }

//    public static class CreateBusinessAdminRequest {
//        @NotBlank public String firstName;
//        @NotBlank public String lastName;
//        @NotBlank @Email public String email;
//        public String mobile;
//        public java.util.UUID b2bUnitId;
//    }

//    public static class AssignBusinessAdminRequest {
//        @NotBlank @Email public String email;
//        public java.util.UUID b2bUnitId;
//    }

//    public static class InviteEmployeeRequest {
//        @NotBlank public String firstName;
//        @NotBlank public String lastName;
//        @NotBlank @Email public String email;
//        public String mobile;
//        @NotEmpty public java.util.List<java.util.UUID> roleIds;
//    }

//    public static class SetupPasswordRequest {
//        @NotBlank public String token;
//        @NotBlank public String newPassword;
//    }

//    public static class IdsRequest {
//        @NotEmpty public List<UUID> ids;
//    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        boolean sent = otpService.sendOtp(request.email);
        if (sent) {
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found with the provided email"));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.email);
        
        try {
            return otpService.verifyOtpAndGetToken(request.email, request.otp)
                    .flatMap(token -> {
                        log.debug("OTP verified, getting user details for: {}", request.email);
                        return userService.findByEmail(request.email)
                                .map(user -> {
                                    try {
                                        List<String> roles = (user.getRoles() == null)
                                                ? List.of()
                                                : user.getRoles().stream()
                                                        .filter(Objects::nonNull)
                                                        .map(r -> r.getName())
                                                        .filter(Objects::nonNull)
                                                        .collect(Collectors.toList());

                                        Map<String, Object> response = new HashMap<>();
                                        response.put("token", token);
                                        response.put("id", user.getId());
                                        response.put("username", user.getUsername());
                                        response.put("email", user.getEmail());
                                        response.put("mobile", user.getMobile());
                                        response.put("roles", roles);

                                        log.info("Successfully authenticated user via OTP: {}", request.email);
                                        return ResponseEntity.ok(response);
                                    } catch (Exception e) {
                                        log.error("Error processing user data for: " + request.email, e);
                                        return ResponseEntity.status(500).body(
                                                Map.of("error", "Error processing user data"));
                                    }
                                });
                    })
                    .orElseGet(() -> {
                        log.warn("Invalid or expired OTP for email: {}", request.email);
                        return ResponseEntity.status(401)
                                .body(Map.of("error", "Invalid or expired OTP"));
                    });
        } catch (Exception e) {
            log.error("Unexpected error during OTP verification for email: " + request.email, e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An unexpected error occurred during OTP verification"));
        }
    }


    public static class SignupRequest {
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        @NotBlank public String password;
    }

//    public static class LoginRequest {
//        @NotBlank public String emailOrMobile;
//        @NotBlank public String password;
//    }

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

    public static class IdsRequest {
        @NotEmpty public List<UUID> ids;
    }

    public static class OtpRequest {
        @NotBlank @Email public String email;
    }

    public static class VerifyOtpRequest {
        @NotBlank @Email public String email;
        @NotBlank @Size(min = 4, max = 8) public String otp;
    }
}
