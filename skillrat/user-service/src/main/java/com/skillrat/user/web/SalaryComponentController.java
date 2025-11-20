package com.skillrat.user.web;

import com.skillrat.user.dto.SalaryComponentDto;
import com.skillrat.user.service.SalaryComponentService;
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
public class SalaryComponentController {

    private final SalaryComponentService service;

    public SalaryComponentController(SalaryComponentService service) {
        this.service = service;
    }

    @PostMapping("/salary-components")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SalaryComponentDto> create(@RequestBody @Valid SalaryComponentDto req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping("/salary-components")
    @PreAuthorize("isAuthenticated()")
    public List<SalaryComponentDto> list() {
        return service.list();
    }

    @PutMapping("/salary-components/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SalaryComponentDto> update(@PathVariable("id") UUID id,
                                                     @RequestBody @Valid SalaryComponentDto req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/salary-components/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
