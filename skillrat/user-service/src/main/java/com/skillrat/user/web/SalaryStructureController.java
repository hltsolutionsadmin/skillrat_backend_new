package com.skillrat.user.web;

import com.skillrat.user.dto.SalaryStructureDtos;
import com.skillrat.user.service.SalaryStructureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Validated
public class SalaryStructureController {

    private final SalaryStructureService service;

    public SalaryStructureController(SalaryStructureService service) { this.service = service; }

    @PostMapping("/salary-structure")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SalaryStructureDtos.StructureResponse> upsert(@RequestBody @Valid SalaryStructureDtos.UpsertRequest req) {
        return ResponseEntity.ok(service.upsert(req));
    }

    @GetMapping("/salary-structure/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SalaryStructureDtos.StructureResponse> get(@PathVariable("employeeId") UUID employeeId) {
        return service.getLatestByEmployee(employeeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
