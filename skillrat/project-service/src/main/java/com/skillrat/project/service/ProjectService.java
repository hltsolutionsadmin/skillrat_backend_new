package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.*;
import com.skillrat.project.client.UserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WBSElementRepository wbsRepository;
    private final ProjectMemberRepository memberRepository;
    private final WBSAllocationRepository allocationRepository;
    private final UserClient userClient;

    public ProjectService(ProjectRepository projectRepository,
                          WBSElementRepository wbsRepository,
                          ProjectMemberRepository memberRepository,
                          WBSAllocationRepository allocationRepository,
                          UserClient userClient) {
        this.projectRepository = projectRepository;
        this.wbsRepository = wbsRepository;
        this.memberRepository = memberRepository;
        this.allocationRepository = allocationRepository;
        this.userClient = userClient;
    }

    @Transactional(readOnly = true)
    public Project getProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @Transactional
    public Project createProject(String name,
                                 String code,
                                 String description,
                                 UUID b2bUnitId,
                                 LocalDate start,
                                 LocalDate end,
                                 String clientName,
                                 String clientPrimaryEmail,
                                 String clientSecondaryEmail) {
        String tenant = TenantContext.getTenantId();
        if (code != null && !code.isBlank() && projectRepository.findByCodeAndTenantId(code, tenant).isPresent()) {
            throw new IllegalStateException("Project code already exists for tenant");
        }
        Project p = new Project();
        p.setName(name);
        p.setCode(code);
        p.setB2bUnitId(b2bUnitId);
        p.setStartDate(start);
        p.setEndDate(end);
        p.setTenantId(tenant);
        p.setDescription(description);
        if (clientName != null && !clientName.isBlank()) {
            ProjectClient client = new ProjectClient();
            client.setName(clientName);
            if (clientPrimaryEmail != null && !clientPrimaryEmail.isBlank()) {
                client.setPrimaryContactEmail(clientPrimaryEmail);
            }
            if (clientSecondaryEmail != null && !clientSecondaryEmail.isBlank()) {
                client.setSecondaryContactEmail(clientSecondaryEmail);
            }
            client.setTenantId(tenant);
            applyAuditFromCurrentUser(client);
            p.setClient(client);
        }
        applyAuditFromCurrentUser(p);
        return projectRepository.save(p);
    }

    @Transactional
    public WBSElement createWbs(UUID projectId, String name, String code, WBSCategory category, LocalDate start, LocalDate end) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        String tenant = TenantContext.getTenantId();
        if (code != null && !code.isBlank()) {
            Optional<WBSElement> existing = wbsRepository.findByCodeAndTenantId(code, tenant);
            if (existing.isPresent()) {
                throw new IllegalStateException("WBS code already exists for tenant");
            }
        }
        WBSElement wbs = new WBSElement();
        wbs.setProject(project);
        wbs.setName(name);
        wbs.setCode(code);
        wbs.setCategory(category == null ? WBSCategory.OTHER : category);
        wbs.setStartDate(start);
        wbs.setEndDate(end);
        wbs.setTenantId(tenant);
        return wbsRepository.save(wbs);
    }

    @Transactional
    public ProjectMember addOrUpdateMember(UUID projectId, UUID employeeId, ProjectRole role, UUID reportingManagerId,
                                           LocalDate start, LocalDate end, boolean active) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        String tenant = TenantContext.getTenantId();
        ProjectMember member = memberRepository.findByProject_IdAndEmployeeId(projectId, employeeId)
                .orElseGet(() -> {
                    ProjectMember m = new ProjectMember();
                    m.setProject(project);
                    m.setEmployeeId(employeeId);
                    m.setTenantId(tenant);
                    return m;
                });
        if (role != null) member.setRole(role);
        member.setReportingManagerId(reportingManagerId);
        member.setStartDate(start);
        member.setEndDate(end);
        member.setActive(active);
        applyAuditFromCurrentUser(member);
        return memberRepository.save(member);
    }

    @Transactional
    public WBSAllocation allocateMemberToWbs(UUID memberId, UUID wbsId, LocalDate start, LocalDate end) {
        ProjectMember member = memberRepository.findByEmployeeId(memberId).stream().findAny()
                .orElseThrow(() -> new IllegalArgumentException("Project member not found"));
        WBSElement wbs = wbsRepository.findById(wbsId)
                .orElseThrow(() -> new IllegalArgumentException("WBS not found"));
        // Validate project match
        if (!wbs.getProject().getId().equals(member.getProject().getId())) {
            throw new IllegalStateException("WBS and Member must belong to the same project");
        }
        // Validate dates within WBS dates, if provided
        if (wbs.getStartDate() != null && start != null && start.isBefore(wbs.getStartDate())) {
            throw new IllegalStateException("Allocation start before WBS start");
        }
        if (wbs.getEndDate() != null && end != null && end.isAfter(wbs.getEndDate())) {
            throw new IllegalStateException("Allocation end after WBS end");
        }
        String tenant = TenantContext.getTenantId();
        WBSAllocation alloc = new WBSAllocation();
        alloc.setMember(member);
        alloc.setWbsElement(wbs);
        alloc.setStartDate(start);
        alloc.setEndDate(end);
        alloc.setActive(true);
        alloc.setTenantId(tenant);
        return allocationRepository.save(alloc);
    }

    private void applyAuditFromCurrentUser(com.skillrat.common.orm.BaseEntity entity) {
        try {
            Map<String, Object> me = userClient.me();
            if (me != null) {
                Object email = me.get("email");
                if (email != null) {
                    String v = Objects.toString(email, null);
                    entity.setCreatedBy(v);
                    entity.setUpdatedBy(v);
                }
            }
        } catch (Exception ignored) {}
        if (entity.getCreatedBy() == null || entity.getUpdatedBy() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String name = auth.getName();
                if (name != null && !name.isBlank()) {
                    if (entity.getCreatedBy() == null) entity.setCreatedBy(name);
                    if (entity.getUpdatedBy() == null) entity.setUpdatedBy(name);
                }
                Object principal = auth.getPrincipal();
                if (principal instanceof Jwt jwt) {
                    String email = jwt.getClaimAsString("email");
                    if (email != null && !email.isBlank()) {
                        entity.setCreatedBy(email);
                        entity.setUpdatedBy(email);
                    }
                }
            }
        }
    }

}
