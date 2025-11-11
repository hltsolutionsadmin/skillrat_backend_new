package com.skillrat.project.web;

import com.skillrat.project.service.SummaryService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/summary")
@Validated
public class SummaryController {

    private final SummaryService service;

    public SummaryController(SummaryService service) { this.service = service; }

    @GetMapping("/week")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> weekly(
            @RequestParam(value = "employeeId", required = false) UUID employeeId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from
    ) {
        UUID eid = employeeId != null ? employeeId : currentUserIdOrNull();
        if (eid == null) return ResponseEntity.badRequest().body(java.util.Map.of("error", "employeeId is required"));
        return ResponseEntity.ok(service.weeklySummary(eid, from));
    }

    @GetMapping("/day")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> myDay(
            @RequestParam(value = "employeeId", required = false) UUID employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        UUID eid = employeeId != null ? employeeId : currentUserIdOrNull();
        if (eid == null) return ResponseEntity.badRequest().body(java.util.Map.of("error", "employeeId is required"));
        return ResponseEntity.ok(service.myDay(eid, date));
    }

    private UUID currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
