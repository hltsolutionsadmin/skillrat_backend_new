package com.skillrat.user.web;

import com.skillrat.user.dto.LeaveDTO;
import com.skillrat.user.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.YearMonth;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@Validated
public class LeaveController {

    private final LeaveService service;

    public LeaveController(LeaveService service) { this.service = service; }

    @PostMapping("/leaves")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveDTO> apply(@RequestBody @Valid LeaveDTO req) {
        return ResponseEntity.ok(service.apply(req));
    }

    @PutMapping("/leaves/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveDTO> approve(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PutMapping("/leaves/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveDTO> reject(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @GetMapping("/leaves/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public List<LeaveDTO> list(@PathVariable("employeeId") UUID employeeId) {
        return service.listByEmployee(employeeId);
    }

    @GetMapping("/leaves/{employeeId}/{month}/{year}/approved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveDTO>> listApprovedOverlapping(@PathVariable("employeeId") UUID employeeId,
                                                                  @PathVariable int month,
                                                                  @PathVariable int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return ResponseEntity.ok(service.listApprovedOverlapping(employeeId, from, to));
    }
}
