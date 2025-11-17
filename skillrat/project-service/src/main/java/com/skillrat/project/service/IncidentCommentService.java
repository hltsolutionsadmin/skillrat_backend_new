package com.skillrat.project.service;

import com.skillrat.project.client.UserClient;
import com.skillrat.project.domain.Incident;
import com.skillrat.project.domain.IncidentComment;
import com.skillrat.project.repo.IncidentCommentRepository;
import com.skillrat.project.repo.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidentCommentService {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final UserClient userClient;

    public IncidentCommentService(IncidentRepository incidentRepository,
                                  IncidentCommentRepository commentRepository, UserClient userClient) {
        this.incidentRepository = incidentRepository;
        this.commentRepository = commentRepository;
        this.userClient = userClient;
    }

    private UUID currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try { return UUID.fromString(auth.getName()); } catch (IllegalArgumentException ex) { return null; }
    }

    @Transactional(readOnly = true)
    public Page<IncidentComment> list(UUID incidentId, Pageable pageable) {
        ensureIncidentExists(incidentId);
        return commentRepository.findByIncident_Id(incidentId, pageable);
    }

    @Transactional
    public IncidentComment add(UUID incidentId, String body) {
        if (body == null || body.isBlank()) throw new IllegalArgumentException("Comment body is required");
        Incident incident = ensureIncidentExists(incidentId);
        IncidentComment c = new IncidentComment();
        c.setIncident(incident);
        c.setBody(body.trim());
        // Try to resolve author from user-service; on failure, fall back to current auth principal
        try {
            Map<String, Object> me = userClient.me();
            if (me != null) {
                Object idObj = me.get("id");
                if (idObj != null) {
                    UUID userId = UUID.fromString(idObj.toString());
                    c.setAuthorId(userId);
                }
            }
        } catch (Exception ex) {
            // Ignore and fall back to security context (may be non-UUID subject)
            UUID actor = currentUserIdOrNull();
            if (actor != null) {
                c.setAuthorId(actor);
            }
        }
        return commentRepository.save(c);
    }

    @Transactional
    public IncidentComment update(UUID incidentId, UUID commentId, String body) {
        if (body == null || body.isBlank()) throw new IllegalArgumentException("Comment body is required");
        ensureIncidentExists(incidentId);
        IncidentComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        UUID userId=null;
        try {
            Map<String, Object> me = userClient.me();
            if (me != null) {
                Object idObj = me.get("id");
                if (idObj != null) {
                    userId = UUID.fromString(idObj.toString());
                }
            }
        } catch (Exception ex) {
            // Ignore and fall back to security context (may be non-UUID subject)
            UUID actor = currentUserIdOrNull();
            if (actor != null) {
                userId=actor;
            }
        }
        if (!userId.equals(c.getAuthorId())) {
            throw new IllegalStateException("Only the author can edit this comment");
        }
        c.setBody(body.trim());
        c.setEditedAt(Instant.now());
        return commentRepository.save(c);
    }

    @Transactional
    public void delete(UUID incidentId, UUID commentId) {
        ensureIncidentExists(incidentId);
        IncidentComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        UUID actor = Optional.ofNullable(currentUserIdOrNull()).orElseThrow(() -> new IllegalArgumentException("Authenticated user id not available"));
        if (!actor.equals(c.getAuthorId())) {
            throw new IllegalStateException("Only the author can delete this comment");
        }
        commentRepository.delete(c);
    }

    @Transactional(readOnly = true)
    public Optional<IncidentComment> get(UUID incidentId, UUID commentId) {
        ensureIncidentExists(incidentId);
        return commentRepository.findById(commentId)
                .filter(c -> c.getIncident() != null && incidentId.equals(c.getIncident().getId()));
    }

    private Incident ensureIncidentExists(UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
    }
}
