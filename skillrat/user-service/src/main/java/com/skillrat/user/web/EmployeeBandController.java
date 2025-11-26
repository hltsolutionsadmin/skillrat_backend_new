package com.skillrat.user.web;

import com.skillrat.user.domain.EmployeeBand;
import com.skillrat.user.domain.EmployeeOrgBand;
import com.skillrat.user.service.EmployeeBandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employeebands")
@RequiredArgsConstructor
public class EmployeeBandController {

    private final EmployeeBandService service;

    @PostMapping("/create")
    public ResponseEntity<EmployeeOrgBand> createBand(@RequestBody EmployeeOrgBand orgBand) {
        EmployeeOrgBand band=new EmployeeOrgBand();
        band.setB2bUnitId(orgBand.getB2bUnitId());
        band.setName(orgBand.getName());
        return ResponseEntity.ok(service.createBand(band));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeOrgBand>> getBandsByB2bUnit(@RequestParam String b2bUnitId) {
        return ResponseEntity.ok(service.getBandsByB2bUnit(b2bUnitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeOrgBand> getBand(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getBand(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeOrgBand> updateBand(
            @PathVariable UUID id,
            @RequestParam EmployeeBand name) {
        return ResponseEntity.ok(service.updateBand(id, name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBand(@PathVariable UUID id) {
        service.deleteBand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/names")
    public ResponseEntity<EmployeeBand[]> getAllBandNames() {
        return ResponseEntity.ok(service.getAllBandNames());
    }
}
