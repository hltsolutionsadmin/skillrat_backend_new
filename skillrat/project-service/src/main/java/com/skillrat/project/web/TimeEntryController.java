package com.skillrat.project.web;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.project.domain.TimeEntry;
import com.skillrat.project.service.TimeEntryService;
import com.skillrat.project.web.dto.TimeEntryCreateRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/time-entries")
@Validated
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeEntry> createDraft(@RequestBody @Valid @NonNull TimeEntryCreateRequest req) {
        TimeEntry te = service.createDraft(req);
        return ResponseEntity.ok(te);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeEntry> submit(@PathVariable("id") @NonNull UUID id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','PMO','PROJECT_MANAGER','TEAM_LEAD')")
    public ResponseEntity<TimeEntry> approve(@PathVariable("id") @NonNull UUID id, @RequestBody Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.approve(id, approverId, note));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','PMO','PROJECT_MANAGER')")
    public ResponseEntity<TimeEntry> reject(@PathVariable("id") @NonNull UUID id, @RequestBody Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.reject(id, approverId, note));
    }

    
}
