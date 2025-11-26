package com.skillrat.project.service;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.client.UserClient;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.IncidentRepository;
import com.skillrat.project.repo.ProjectRepository;
import com.skillrat.project.repo.IncidentCategoryRepository;
import com.skillrat.project.repo.IncidentSubCategoryRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final ProjectRepository projectRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final IncidentSubCategoryRepository subCategoryRepository;
    private final EntityManager entityManager;
    private final AuditClient auditClient;
    private final UserClient userClient;
    private final IncidentMediaService incidentMediaService;

    public IncidentService(ProjectRepository projectRepository,
                           IncidentRepository incidentRepository,
                           IncidentCategoryRepository categoryRepository,
                           IncidentSubCategoryRepository subCategoryRepository,
                           EntityManager entityManager,
                           AuditClient auditClient,
                           UserClient userClient,
                           IncidentMediaService incidentMediaService) {
        this.projectRepository = projectRepository;
        this.incidentRepository = incidentRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.entityManager = entityManager;
        this.auditClient = auditClient;
        this.userClient = userClient;
        this.incidentMediaService = incidentMediaService;
    }

    @Transactional
    public Incident create(UUID projectId,
                           String title,
                           String shortDescription,
                           IncidentUrgency urgency,
                           IncidentImpact impact,
                           UUID categoryId,
                           UUID subCategoryId,
                           List<MultipartFile> mediaFiles,
                           List<String> mediaUrls, UUID assigneeId, UUID reporterId) throws Exception {
        log.info("Creating incident with projectId: {}, title: {}", projectId, title);

        // Validate inputs
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        String tenantId = TenantContext.getTenantId();

        // Validate category
        IncidentCategoryEntity category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        // Validate subcategory if provided
        IncidentSubCategoryEntity subCategory = null;
        if (subCategoryId != null) {
            subCategory = subCategoryRepository.findByIdAndTenantId(subCategoryId, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("SubCategory not found with id: " + subCategoryId));
            if (!Objects.equals(subCategory.getCategory().getId(), category.getId())) {
                throw new IllegalArgumentException("SubCategory does not belong to the specified Category");
            }
        }

        // Create and save the incident
        Incident incident = new Incident();
        incident.setProject(project);
        incident.setIncidentNumber(generateIncidentNumber(project));
        incident.setTitle(title);
        incident.setShortDescription(shortDescription);
        incident.setUrgency(urgency);
        incident.setImpact(impact);
        incident.setPriority(computePriority(urgency, impact));
        incident.setCategory(category);
        incident.setSubCategory(subCategory);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setTenantId(tenantId);

        if(assigneeId != null) {
            incident.setAssigneeId(assigneeId);
            incident.setAssigneeName(userClient.getUserById(assigneeId).getFirstName() + " " + userClient.getUserById(assigneeId).getLastName());
        }
        if(reporterId != null) {
            incident.setReporterId(reporterId);
            incident.setReporterName(userClient.getUserById(reporterId).getFirstName() + " " + userClient.getUserById(reporterId).getLastName());
        }
        // Save the incident first to get an ID
        Incident saved = incidentRepository.save(incident);
        log.info("Incident created id={}, projectId={}, number={}",
                saved.getId(), projectId, saved.getIncidentNumber());

        try {
            // Handle media files and URLs
            List<MediaModel> savedMedia = incidentMediaService.handleIncidentMedia(saved, mediaFiles, mediaUrls);
            saved.getMedia().addAll(savedMedia);
            saved = incidentRepository.save(saved);

        } catch (Exception e) {
            log.error("Error processing media for incident: {}", saved.getId(), e);
            // Don't fail the entire request if media processing fails
        }

        // Log the change
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

        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        return  (project.getCode() != null && !project.getCode().isBlank())
                ? project.getCode()
                : "INC"+ DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
    }


    @Transactional(readOnly = true)
    public Page<Incident> listByProject(UUID projectId, Pageable pageable) {
        return incidentRepository.findByProject_Id(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Incident> listByProjectFiltered(
            UUID projectId,
            IncidentPriority priority,
            UUID categoryId,
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
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
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
    public Incident assignAssignee(UUID incidentId, UUID assigneeId) throws Exception {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        UUID oldAssignee = incident.getAssigneeId();
        UserDTO user=userClient.getUserById(assigneeId);
        if(user==null){
            throw new Exception("User not found with id: " + assigneeId);
        }
        incident.setAssigneeId(assigneeId);
        incident.setAssigneeName(user.getFirstName()+" "+user.getLastName());
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
    public Incident assignReporter(UUID incidentId, UUID reporterId) throws Exception {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        UUID oldReporter = incident.getReporterId();
        UserDTO user=userClient.getUserById(reporterId);
        if(user==null){
            throw new Exception("User not found with id: " + reporterId);
        }
        incident.setReporterId(reporterId);
        incident.setAssigneeName(user.getFirstName()+" "+user.getLastName());

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
    public Incident updateStatus(UUID incidentId, IncidentStatus status, @NotBlank(message = "Title is required") String discription, @NotNull(message = "Urgency is required") IncidentUrgency urgency, @NotNull(message = "Impact is required") IncidentImpact impact, List<MultipartFile> mediaFiles, List<String> mediaUrls) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));


        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(status);
        incident.setShortDescription(discription);
        if (urgency != null) {
            incident.setUrgency(urgency);
        }
        if (impact != null) {
            incident.setImpact(impact);
        }
        if (impact != null && urgency != null) {
            incident.setPriority(computePriority(urgency, impact));
        }
        Incident saved = incidentRepository.save(incident);
        log.info("Incident created id={}, projectId={}, number={}",
                saved.getId(), saved.getProject().getId(), saved.getIncidentNumber());
        if (mediaFiles != null && mediaFiles.size() > 0) {
            try {
                // Handle media files and URLs
                List<MediaModel> savedMedia = incidentMediaService.handleIncidentMedia(saved, mediaFiles, mediaUrls);
                saved.getMedia().addAll(savedMedia);
                saved = incidentRepository.save(saved);

            } catch (Exception e) {
                log.error("Error processing media for incident: {}", saved.getId(), e);
                // Don't fail the entire request if media processing fails
            }

        }
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
    public Page<Incident> listByReporter(UUID projectId, Pageable pageable) {

        Map<String, Object> me = userClient.me();
        if (me == null || me.get("id") == null) {
            throw new IllegalStateException("Logged-in user not found");
        }

        UUID loggedInUserId = UUID.fromString(me.get("id").toString());

        Specification<Incident> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("project").get("id"), projectId),
                cb.equal(root.get("reporterId"), loggedInUserId)
        );

        return incidentRepository.findAll(spec, pageable);
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
