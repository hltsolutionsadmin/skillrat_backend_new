package com.skillrat.placement.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.placement.domain.Application;
import com.skillrat.placement.domain.ApplicationStatus;
import com.skillrat.placement.domain.Opening;
import com.skillrat.placement.domain.OpeningType;
import com.skillrat.placement.service.OpeningService;
import com.skillrat.placement.service.UserLookupClient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/openings")
@Validated
public class OpeningController {

    private final OpeningService openingService;
    private final UserLookupClient userLookupClient;

    public OpeningController(OpeningService openingService, UserLookupClient userLookupClient) {
        this.openingService = openingService;
        this.userLookupClient = userLookupClient;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_HEAD','HR_RECRUITER','HR_MANAGER','ADMIN')")
    public ResponseEntity<Opening> create(@RequestBody @Validated CreateOpeningRequest req, Authentication auth) {
        String email = auth.getName();
        UUID b2bUnitId = userLookupClient.findB2bUnitIdByEmail(email).orElseThrow(() -> new IllegalArgumentException("User business not found"));
        UUID createdByUserId = null; // optional enhancement: resolve user id as well
        Opening o = openingService.createOpening(b2bUnitId, createdByUserId, req.title, req.description, req.type, req.location);
        return ResponseEntity.ok(o);
    }

    @GetMapping("/my-business")
    @PreAuthorize("isAuthenticated()")
    public List<Opening> myBusiness(Authentication auth) {
        String email = auth.getName();
        UUID b2bUnitId = userLookupClient.findB2bUnitIdByEmail(email).orElseThrow(() -> new IllegalArgumentException("User business not found"));
        return openingService.listForBusiness(b2bUnitId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Opening> get(@PathVariable("id") @NonNull UUID id) {
        return openingService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Application> apply(@PathVariable("id") UUID openingId, @RequestBody @Validated ApplyRequest req, Authentication auth) {
        UUID submittedBy = auth != null && auth.isAuthenticated() ? null : null; // could map to user id if needed
        Application app = openingService.apply(openingId, req.name, req.email, req.phone, req.resumeUrl, submittedBy);
        return ResponseEntity.ok(app);
    }

    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('HR_HEAD','HR_RECRUITER','HR_MANAGER','ADMIN')")
    public List<Application> applications(@PathVariable("id") UUID openingId) {
        return openingService.listApplications(openingId);
    }

    @PostMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('HR_HEAD','HR_RECRUITER','HR_MANAGER','ADMIN')")
    public ResponseEntity<?> setStatus(@PathVariable("id") @NonNull UUID applicationId, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "");
        ApplicationStatus st;
        try {
            st = ApplicationStatus.valueOf(status);
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", "Invalid status")); }
        return openingService.setApplicationStatus(applicationId, st)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static class CreateOpeningRequest {
        @NotBlank public String title;
        @NotBlank public String description;
        @NotNull public OpeningType type; // JOB/INTERNSHIP
        public String location;
    }

    public static class ApplyRequest {
        @NotBlank public String name;
        @NotBlank @Email public String email;
        public String phone;
        public String resumeUrl;
    }
}
