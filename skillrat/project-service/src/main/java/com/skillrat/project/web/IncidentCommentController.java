package com.skillrat.project.web;

import com.skillrat.project.domain.IncidentComment;
import com.skillrat.project.service.IncidentCommentService;
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
    public Page<IncidentComment> list(@PathVariable("incidentId") UUID incidentId, Pageable pageable) {
        return service.list(incidentId, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> add(@PathVariable("incidentId") UUID incidentId, @RequestBody CreateOrUpdate req) {
        IncidentComment c = service.add(incidentId, req.body);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> get(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id) {
        return service.get(incidentId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> update(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id, @RequestBody CreateOrUpdate req) {
        IncidentComment c = service.update(incidentId, id, req.body);
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable("incidentId") UUID incidentId, @PathVariable("id") UUID id) {
        service.delete(incidentId, id);
        return ResponseEntity.noContent().build();
    }

    public static class CreateOrUpdate {
        @NotBlank public String body;
    }
}
