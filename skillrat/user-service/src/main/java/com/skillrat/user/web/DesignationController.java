package com.skillrat.user.web;

import com.skillrat.user.domain.Designation;
import com.skillrat.user.dto.DesignationDTO;
import com.skillrat.user.dto.DesignationRequestDTO;
import com.skillrat.user.dto.DesignationResponseDTO;
import com.skillrat.user.service.DesignationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<Designation> updateDesignation(@PathVariable UUID id,
                                                         @RequestBody @Valid DesignationRequestDTO request) {
        return ResponseEntity.ok(designationService.updateDesignation(id, request));
    }

    // Delete designation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignation(@PathVariable UUID id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.noContent().build();
    }
}
