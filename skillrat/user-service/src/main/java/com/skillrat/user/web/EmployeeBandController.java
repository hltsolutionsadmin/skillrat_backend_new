package com.skillrat.user.web;

import com.skillrat.user.domain.EmployeeOrgBand;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.service.EmployeeBandService;
import com.skillrat.user.web.dto.EmployeeBandRequestDTO;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employeebands")
@RequiredArgsConstructor
public class EmployeeBandController {

    private final EmployeeBandService service;

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<EmployeeOrgBand> createBand(@Valid @RequestBody EmployeeBandRequestDTO bandDTO) {
        EmployeeOrgBand band = new EmployeeOrgBand();
        band.setName(bandDTO.getName());
        band.setExperienceMin(bandDTO.getExperienceMin());
        band.setExperienceMax(bandDTO.getExperienceMax());
        band.setSalary(bandDTO.getSalary());
        
        // Create a B2BUnit with just the ID - the service will handle fetching the full entity
        B2BUnit b2bUnit = new B2BUnit();
        b2bUnit.setId(bandDTO.getB2bUnitId());
        band.setB2bUnit(b2bUnit);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBand(band));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeOrgBand>> getBandsByB2bUnit(@RequestParam UUID b2bUnitId) {
        return ResponseEntity.ok(service.getBandsByB2bUnit(b2bUnitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeOrgBand> getBand(@PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(service.getBand(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeOrgBand> updateBand(
            @PathVariable @NonNull UUID id,
            @RequestBody EmployeeOrgBand orgBand) {
        return ResponseEntity.ok(service.updateBand(id, orgBand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBand(@PathVariable @NonNull UUID id) {
        service.deleteBand(id);
        return ResponseEntity.noContent().build();
    }
}
