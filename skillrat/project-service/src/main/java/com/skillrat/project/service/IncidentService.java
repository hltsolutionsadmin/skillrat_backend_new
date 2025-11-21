package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.client.UserClient;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.IncidentRepository;
import com.skillrat.project.repo.ProjectRepository;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final ProjectRepository projectRepository;
    private final IncidentRepository incidentRepository;
    private final EntityManager entityManager;
    private final AuditClient auditClient;
    private final UserClient userClient;

    public IncidentService(ProjectRepository projectRepository,
                           IncidentRepository incidentRepository,
                           EntityManager entityManager,
                           AuditClient auditClient, UserClient userClient) {
        this.projectRepository = projectRepository;
        this.incidentRepository = incidentRepository;
        this.entityManager = entityManager;
        this.auditClient = auditClient;
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
        incident.setTenantId(TenantContext.getTenantId());
        Incident saved = incidentRepository.save(incident);
        log.info("Incident created id={}, projectId={}, number={}, createdBy={}",
                saved.getId(), projectId, saved.getIncidentNumber(), saved.getCreatedBy());
        auditClient.logChange(
                "Incident",
                saved.getId(),
                "CREATE",
                null,
                null,
                null,
                saved.getCreatedBy()
        );
        return saved;
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
    public Page<Incident> listByProjectFiltered(
            UUID projectId,
            IncidentPriority priority,
            IncidentCategory category,
            IncidentStatus status,
            String search,
            Pageable pageable
    ) {
        Specification<Incident> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("project").get("id"), projectId));
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                var orPred = cb.or(
                        cb.like(cb.lower(root.get("incidentNumber")), like),
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("shortDescription")), like)
                );
                predicates.add(orPred);
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return incidentRepository.findAll(spec, pageable);
    }

    @Transactional
    public Incident assignAssignee(UUID incidentId, UUID assigneeId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        UUID oldAssignee = incident.getAssigneeId();
        incident.setAssigneeId(assigneeId);
        Incident saved = incidentRepository.save(incident);
        log.info("Incident assignee updated id={}, oldAssigneeId={}, newAssigneeId={}, updatedBy={}",
                saved.getId(),
                oldAssignee != null ? oldAssignee : null,
                assigneeId,
                saved.getUpdatedBy());
        auditClient.logChange(
                "Incident",
                saved.getId(),
                "UPDATE",
                "assigneeId",
                oldAssignee != null ? oldAssignee.toString() : null,
                assigneeId != null ? assigneeId.toString() : null,
                saved.getUpdatedBy()
        );
        return saved;
    }

    @Transactional
    public Incident assignReporter(UUID incidentId, UUID reporterId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        UUID oldReporter = incident.getReporterId();
        incident.setReporterId(reporterId);
        Incident saved = incidentRepository.save(incident);
        log.info("Incident reporter updated id={}, oldReporterId={}, newReporterId={}, updatedBy={}",
                saved.getId(),
                oldReporter != null ? oldReporter : null,
                reporterId,
                saved.getUpdatedBy());
        auditClient.logChange(
                "Incident",
                saved.getId(),
                "UPDATE",
                "reporterId",
                oldReporter != null ? oldReporter.toString() : null,
                reporterId != null ? reporterId.toString() : null,
                saved.getUpdatedBy()
        );
        return saved;
    }

    @Transactional
    public Incident updateStatus(UUID incidentId, IncidentStatus status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(status);
        Incident saved = incidentRepository.save(incident);
        log.info("Incident status updated id={}, oldStatus={}, newStatus={}, updatedBy={}",
                saved.getId(),
                oldStatus,
                status,
                saved.getUpdatedBy());
        auditClient.logChange(
                "Incident",
                saved.getId(),
                "UPDATE",
                "status",
                oldStatus != null ? oldStatus.name() : null,
                status != null ? status.name() : null,
                saved.getUpdatedBy()
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Incident> history(UUID incidentId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(Incident.class, incidentId);
        log.debug("Fetched {} revisions for incident id={}", revisions.size(), incidentId);
        return revisions.stream()
                .map(rev -> reader.find(Incident.class, incidentId, rev))
                .toList();
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

    @Transactional(readOnly = true)
    public Page<Incident> listByProjectAndLoggedInUser(UUID projectId, Pageable pageable) {

        Map<String, Object> me = userClient.me();
        if (me == null || me.get("id") == null) {
            throw new IllegalStateException("Logged-in user not found");
        }

        UUID loggedInUserId = UUID.fromString(me.get("id").toString());

        Specification<Incident> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("project").get("id"), projectId),
                cb.equal(root.get("assigneeId"), loggedInUserId)
        );

        return incidentRepository.findAll(spec, pageable);
    }

}
