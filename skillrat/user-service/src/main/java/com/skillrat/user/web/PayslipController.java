package com.skillrat.user.web;

import com.skillrat.user.dto.PayslipDTO;
import com.skillrat.user.service.PayrollService;
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
public class PayslipController {

    private final PayrollService payrollService;

    public PayslipController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/payslips/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayslipDTO> generate(@RequestBody @Valid PayslipDTO req) {
        return ResponseEntity.ok(payrollService.generate(req));
    }

    @GetMapping("/payslips/{employeeId}/{month}/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayslipDTO> get(@PathVariable UUID employeeId,
                                                          @PathVariable int month,
                                                          @PathVariable int year) {
        return payrollService.get(employeeId, month, year)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payslips/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public List<PayslipDTO> list(@PathVariable UUID employeeId) {
        return payrollService.listByEmployee(employeeId);
    }
}
