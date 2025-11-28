package com.skillrat.project.web;

import com.skillrat.project.service.IncidentCommentService;
import com.skillrat.project.dto.request.IncidentCommentRequest;
import com.skillrat.project.dto.response.IncidentCommentResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents/{incidentId}/comments")
@Validated
public class IncidentCommentController {

    private final IncidentCommentService service;

    public IncidentCommentController(IncidentCommentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<IncidentCommentResponse> list(@PathVariable("incidentId") UUID incidentId, Pageable pageable) {
        return service.list(incidentId, pageable).map(service::toDto);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentCommentResponse> add(@PathVariable("incidentId") UUID incidentId, @RequestBody IncidentCommentRequest req) {
        var c = service.add(incidentId, req.getComment());
        return ResponseEntity.ok(service.toDto(c));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentCommentResponse> get(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id) {
        return service.get(incidentId, id)
                .map(service::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentCommentResponse> update(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id, @RequestBody IncidentCommentRequest req) {
        var c = service.update(incidentId, id, req.getComment());
        return ResponseEntity.ok(service.toDto(c));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id) {
        service.delete(incidentId, id);
        return ResponseEntity.noContent().build();
    }
}
