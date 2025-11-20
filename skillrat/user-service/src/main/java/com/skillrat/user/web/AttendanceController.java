package com.skillrat.user.web;

import com.skillrat.user.dto.AttendanceDTO;
import com.skillrat.user.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) { this.service = service; }

    @PostMapping("/attendance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceDTO> mark(@RequestBody @Valid AttendanceDTO req) {
        return ResponseEntity.ok(service.mark(req));
    }

    @GetMapping("/attendance/{employeeId}/{month}/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceDTO>> getMonth(@PathVariable UUID employeeId,
                                                        @PathVariable int month,
                                                        @PathVariable int year) {
        return ResponseEntity.ok(service.getMonth(employeeId, month, year));
    }
}
