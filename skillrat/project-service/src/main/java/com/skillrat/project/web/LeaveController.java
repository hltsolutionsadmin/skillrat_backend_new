package com.skillrat.project.web;

import com.skillrat.project.domain.LeaveRequest;
import com.skillrat.project.domain.LeaveType;
import com.skillrat.project.domain.LeaveStatus;
import com.skillrat.project.service.LeaveService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
@Validated
public class LeaveController {

    private final LeaveService service;

    public LeaveController(LeaveService service) { this.service = service; }

    // Employee raises a leave request
    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveRequest> request(@RequestBody @Valid LeaveRequestDTO req) {
        LeaveRequest lr = service.request(req.employeeId, req.b2bUnitId, req.type, req.fromDate, req.toDate, req.perDayHours, req.note);
        return ResponseEntity.ok(lr);
    }

    // Approve by reporting manager/HR/PMO/ADMIN
    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveRequest> approve(@PathVariable("id") UUID id, @RequestBody(required = false) Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.approve(id, approverId, note));
    }

    // Reject by reporting manager/HR/PMO/ADMIN
    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveRequest> reject(@PathVariable("id") UUID id, @RequestBody(required = false) Map<String, String> body) {
        UUID approverId = body != null && body.get("approverId") != null ? UUID.fromString(body.get("approverId")) : null;
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(service.reject(id, approverId, note));
    }

    @GetMapping("/approved/{employeeId}/{month}/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ApprovedLeaveDTO>> approvedOverlapping(@PathVariable("employeeId") UUID employeeId,
                                                                      @PathVariable("month") int month,
                                                                      @PathVariable("year") int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        List<LeaveRequest> overlapApproved = service.findApprovedOverlapping(employeeId, from, to);
        List<ApprovedLeaveDTO> resp = overlapApproved.stream()
                .map(lr -> new ApprovedLeaveDTO(lr.getType(), lr.getFromDate(), lr.getToDate(), lr.getStatus()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    public record ApprovedLeaveDTO(LeaveType leaveType, LocalDate startDate, LocalDate endDate, LeaveStatus status) {}

    public static class LeaveRequestDTO {
        @NotNull public UUID employeeId;
        @NotNull public UUID b2bUnitId;
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) public LocalDate fromDate;
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) public LocalDate toDate;
        public LeaveType type;
        public BigDecimal perDayHours;
        public String note;
    }
}
