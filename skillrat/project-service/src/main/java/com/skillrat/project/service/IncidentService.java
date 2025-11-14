package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.IncidentRepository;
import com.skillrat.project.repo.ProjectRepository;
import com.skillrat.project.client.UserClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class IncidentService {

    private final ProjectRepository projectRepository;
    private final IncidentRepository incidentRepository;
    private final UserClient userClient;

    public IncidentService(ProjectRepository projectRepository, IncidentRepository incidentRepository, UserClient userClient) {
        this.projectRepository = projectRepository;
        this.incidentRepository = incidentRepository;
        this.userClient = userClient;
    }

    @Transactional
    public Incident create(UUID projectId,
                           String title,
                           String shortDescription,
                           IncidentUrgency urgency,
                           IncidentImpact impact,
                           IncidentCategory category,
                           String subCategory) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Incident incident = new Incident();
        incident.setCreatedDate(Instant.now());
        incident.setUpdatedDate(Instant.now());
        incident.setProject(project);
        incident.setIncidentNumber(generateIncidentNumber(project));
        incident.setTitle(title);
        incident.setShortDescription(shortDescription);
        incident.setUrgency(urgency);
        incident.setImpact(impact);
        incident.setPriority(computePriority(urgency, impact));
        incident.setCategory(category == null ? IncidentCategory.OTHER : category);
        incident.setSubCategory(subCategory);
        incident.setStatus(IncidentStatus.OPEN);
        try {
            Map<String, Object> me = userClient.me();
            if (me != null) {
                Object email = me.get("email");
                if (email != null) {
                    incident.setCreatedBy(Objects.toString(email, null));
                }
                Object id = me.get("id");
                if (id != null) {
                    // Optionally set reporter as current user by default
                    try { incident.setReporterId(UUID.fromString(id.toString())); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        // Fallback to SecurityContext if Feign is unavailable or did not provide email
        if (incident.getCreatedBy() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String name = auth.getName();
                if (name != null && !name.isBlank()) {
                    incident.setCreatedBy(name);
                    incident.setUpdatedBy(name);
                }
                Object principal = auth.getPrincipal();
                if (principal instanceof Jwt jwt) {
                    String email = jwt.getClaimAsString("email");
                    if (email != null && !email.isBlank()) {
                        incident.setCreatedBy(email);
                        incident.setUpdatedBy(email);
                    }
                }
            }
        }
        incident.setTenantId(TenantContext.getTenantId());
        return incidentRepository.save(incident);
    }

    private String generateIncidentNumber(Project project) {
        String prefix = (project.getCode() != null && !project.getCode().isBlank()) ? project.getCode() : "INC";
        Incident last = incidentRepository.findTopByProjectAndIncidentNumberStartingWithOrderByIncidentNumberDesc(project, prefix);
        int nextNumber = 1;
        if (last != null) {
            String lastNumber = last.getIncidentNumber();
            String numericPart = lastNumber.substring(prefix.length());
            try {
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (NumberFormatException ignored) {
                nextNumber = 1;
            }
        }
        return prefix + String.format("%04d", nextNumber);
    }

    @Transactional(readOnly = true)
    public Page<Incident> listByProject(UUID projectId, Pageable pageable) {
        return incidentRepository.findByProject_Id(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Incident getById(UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
    }

    @Transactional(readOnly = true)
    public Page<Incident> listByAssignee(UUID assigneeId, Pageable pageable) {
        return incidentRepository.findByAssigneeId(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Incident> listByReporter(UUID reporterId, Pageable pageable) {
        return incidentRepository.findByReporterId(reporterId, pageable);
    }

    @Transactional
    public Incident assignAssignee(UUID incidentId, UUID assigneeId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setAssigneeId(assigneeId);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident assignReporter(UUID incidentId, UUID reporterId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setReporterId(reporterId);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident updateStatus(UUID incidentId, IncidentStatus status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    private IncidentPriority computePriority(IncidentUrgency urgency, IncidentImpact impact) {
        if (urgency == null) urgency = IncidentUrgency.LOW;
        if (impact == null) impact = IncidentImpact.LOW;
        if (urgency == IncidentUrgency.CRITICAL || impact == IncidentImpact.CRITICAL) {
            return IncidentPriority.CRITICAL;
        }
        int score = level(urgency) + level(impact);
        if (score >= 4) return IncidentPriority.HIGH;
        if (score >= 2) return IncidentPriority.MEDIUM;
        return IncidentPriority.LOW;
    }

    private int level(Enum<?> e) {
        switch (e.name()) {
            case "CRITICAL": return 3;
            case "HIGH": return 2;
            case "MEDIUM": return 1;
            default: return 0;
        }
    }
}
