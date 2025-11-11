package com.skillrat.project.web;

import com.skillrat.project.domain.TimeEntry;
import com.skillrat.project.service.TimeEntryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<TimeEntry> createDraft(@RequestBody @Valid CreateRequest req) {
        TimeEntry te = service.createDraft(req.projectId, req.wbsId, req.memberId, req.employeeId, req.workDate, req.hours, req.notes);
        return ResponseEntity.ok(te);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeEntry> submit(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','PMO','PROJECT_MANAGER','TEAM_LEAD')")
    public ResponseEntity<TimeEntry> approve(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.approve(id, approverId, note));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','PMO','PROJECT_MANAGER')")
    public ResponseEntity<TimeEntry> reject(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.reject(id, approverId, note));
    }

    public static class CreateRequest {
        @NotNull public UUID projectId;
        @NotNull public UUID wbsId;
        @NotNull public UUID memberId;
        @NotNull public UUID employeeId;
        @NotNull public LocalDate workDate;
        @NotNull public BigDecimal hours;
        public String notes;
    }
}
