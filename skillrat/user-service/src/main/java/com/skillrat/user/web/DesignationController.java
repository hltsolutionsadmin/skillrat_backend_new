package com.skillrat.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.user.domain.Designation;
import com.skillrat.user.dto.DesignationDTO;
import com.skillrat.user.dto.DesignationRequestDTO;
import com.skillrat.user.service.DesignationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/designations")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    // List designations with band count
    @GetMapping("/list/{b2bUnitId}")
    public ResponseEntity<List<DesignationDTO>> listDesignations(
            @PathVariable UUID b2bUnitId
    ) {
        return ResponseEntity.ok(designationService.getDesignations(b2bUnitId));
    }

    @GetMapping("/all/{b2bUnitId}")
    public ResponseEntity<List<DesignationDTO>> allDesignations(
            @PathVariable UUID b2bUnitId
    ) {
        return ResponseEntity.ok(designationService.getAllDesignations(b2bUnitId));
    }

    // Create designation
    @PostMapping
    public ResponseEntity<Designation> createDesignation(@RequestBody @Valid DesignationRequestDTO request) {
        return ResponseEntity.ok(designationService.createDesignation(request));
    }

    // Update designation
    @PutMapping("/{id}")
    public ResponseEntity<Designation> updateDesignation(@PathVariable @NonNull UUID id,
                                                         @RequestBody @Valid DesignationRequestDTO request) {
        return ResponseEntity.ok(designationService.updateDesignation(id, request));
    }

    // Delete designation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignation(@PathVariable @NonNull UUID id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.noContent().build();
    }
}
