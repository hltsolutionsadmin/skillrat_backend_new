package com.skillrat.project.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.project.domain.IncidentComment;
import com.skillrat.project.service.IncidentCommentService;

import jakarta.validation.constraints.NotBlank;

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
    public Page<IncidentComment> list(@PathVariable("incidentId") @NonNull UUID incidentId, Pageable pageable) {
        return service.list(incidentId, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> add(@PathVariable("incidentId") @NonNull UUID incidentId, @RequestBody CreateOrUpdate req) {
        IncidentComment c = service.add(incidentId, req.body);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> get(@PathVariable("incidentId") @NonNull UUID incidentId, @PathVariable("id") @NonNull UUID id) {
        return service.get(incidentId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentComment> update(@PathVariable("incidentId") @NonNull UUID incidentId, @PathVariable("id") @NonNull UUID id, @RequestBody CreateOrUpdate req) {
        IncidentComment c = service.update(incidentId, id, req.body);
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable("incidentId") @NonNull UUID incidentId, @PathVariable("id") @NonNull UUID id) {
        service.delete(incidentId, id);
        return ResponseEntity.noContent().build();
    }

    public static class CreateOrUpdate {
        @NotBlank public String body;
    }
}
