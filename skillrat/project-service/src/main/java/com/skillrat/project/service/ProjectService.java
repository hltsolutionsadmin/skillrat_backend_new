package com.skillrat.project.service;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.client.UserClient;
import com.skillrat.project.domain.*;
import com.skillrat.project.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

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

    @Transactional
    public Project createProject(String name,
                               String code,
                               String description,
                               String b2bUnitId,
                               LocalDate start,
                               LocalDate end,
                               String clientName,
                               String clientPrimaryEmail,
                               String clientSecondaryEmail,
                               String createdBy) {
    	String tenant = Optional.ofNullable(TenantContext.getTenantId())
                .filter(t -> !t.isBlank())
                .orElse("default");
        if (code != null && !code.isBlank() && projectRepository.findByCodeAndTenantId(code, tenant).isPresent()) {
            throw new IllegalStateException("Project code already exists for tenant");
        }
        Optional<Project> project = projectRepository.findByCode(code);
        if (project.isPresent()) {
            throw new IllegalStateException("Project code already exists");
        }
        Project p = new Project();
        p.setName(name);
        p.setCode(code);
        p.setB2bUnitId(UUID.fromString(normalizeUuidString(b2bUnitId)));
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
        p.setCreatedBy(createdBy);
        p.setUpdatedBy(createdBy);
        return projectRepository.save(p);
    }

    @Transactional
    public Project updateProject(UUID projectId,
                                 String name,
                                 String code,
                                 String description,
                                 LocalDate start,
                                 LocalDate end,
                                 String clientName,
                                 String clientPrimaryEmail,
                                 String clientSecondaryEmail,
                                 String updatedBy) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        String tenant = Optional.ofNullable(TenantContext.getTenantId()).filter(t -> !t.isBlank()).orElse("default");
        if (code != null && !code.isBlank()) {
            Optional<Project> dup = projectRepository.findByCodeAndTenantId(code, tenant);
            if (dup.isPresent() && !dup.get().getId().equals(projectId)) {
                throw new IllegalStateException("Project code already exists for tenant");
            }
            p.setCode(code);
        }

        if (name != null && !name.isBlank()) p.setName(name);
        if (description != null) p.setDescription(description);
        if (start != null) p.setStartDate(start);
        if (end != null) p.setEndDate(end);

        if (clientName != null || clientPrimaryEmail != null || clientSecondaryEmail != null) {
            ProjectClient client = p.getClient();
            if (client == null && (clientName != null && !clientName.isBlank())) {
                client = new ProjectClient();
                client.setTenantId(tenant);
                p.setClient(client);
            }
            if (client != null) {
                if (clientName != null && !clientName.isBlank()) client.setName(clientName);
                if (clientPrimaryEmail != null) client.setPrimaryContactEmail(clientPrimaryEmail);
                if (clientSecondaryEmail != null) client.setSecondaryContactEmail(clientSecondaryEmail);
            }
        }

        if (updatedBy != null && !updatedBy.isBlank()) p.setUpdatedBy(updatedBy);

        return projectRepository.save(p);
    }

    private String normalizeUuidString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("b2bUnitId is required");
        }
        String s = raw.trim();
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }
        // if already in canonical UUID format, just return
        if (s.contains("-")) {
            return s;
        }
        // handle 32 hex chars without dashes
        if (s.length() == 32) {
            StringBuilder sb = new StringBuilder();
            sb.append(s, 0, 8).append('-')
              .append(s, 8, 12).append('-')
              .append(s, 12, 16).append('-')
              .append(s, 16, 20).append('-')
              .append(s, 20, 32);
            return sb.toString();
        }
        return s;
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
            Optional<WBSElement> existingwithCode = wbsRepository.findByCode(code);
            if (existingwithCode.isPresent()) {
                throw new IllegalStateException("WBS code already exists");
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
    public WBSElement updateWbs(UUID wbsId, String name, String code, WBSCategory category, LocalDate start, LocalDate end,UUID projectId) {
        WBSElement wbs = wbsRepository.findById(wbsId)
                .orElseThrow(() -> new IllegalArgumentException("WBS not found"));
        String tenant = TenantContext.getTenantId();
        if (code != null && !code.isBlank()) {
            Optional<WBSElement> existingTenant = wbsRepository.findByCodeAndTenantId(code, tenant);
            if (existingTenant.isPresent() && !existingTenant.get().getId().equals(wbsId)) {
                throw new IllegalStateException("WBS code already exists for tenant");
            }
            Optional<WBSElement> existingAny = wbsRepository.findByCode(code);
            if (existingAny.isPresent() && !existingAny.get().getId().equals(wbsId)) {
                throw new IllegalStateException("WBS code already exists");
            }
            wbs.setCode(code);
        }
        if (name != null && !name.isBlank()) wbs.setName(name);
        if (category != null) wbs.setCategory(category);
        if (start != null) wbs.setStartDate(start);
        if (end != null) wbs.setEndDate(end);
        if (projectId != null) wbs.setProject(projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));

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
    public WBSAllocation allocateMemberToWbs(UUID projectId,UUID userId, UUID wbsId, LocalDate start, LocalDate end) {
        Optional<ProjectMember> projectMember = memberRepository.findByProject_IdAndEmployeeId(projectId, userId);
        if (projectMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found");
        }
        ProjectMember member = projectMember.get();
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

    @Transactional(readOnly = true)
    public Project getProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @Transactional(readOnly = true)
    public WBSElement getWbs(UUID id) {
        return wbsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WBS not found"));
    }

    @Transactional(readOnly = true)
    public Page<WBSElement> listWbs(UUID projectId, Pageable pageable) {
        return wbsRepository.findByProject_Id(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProjectMember> listMembersByProject(UUID projectId) {
        return memberRepository.findByProject_Id(projectId);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> listMemberUsers(UUID projectId) {
        List<ProjectMember> members = memberRepository.findByProject_Id(projectId);
        if (members == null || members.isEmpty()) return java.util.Collections.emptyList();
        List<UUID> ids = members.stream()
                .map(ProjectMember::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        if (ids.isEmpty()) return java.util.Collections.emptyList();
        java.util.Map<String, java.util.List<UUID>> body = java.util.Map.of("ids", ids);
        return userClient.getUsersByIds(body);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID employeeId) {
        ProjectMember member = memberRepository.findByProject_IdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found"));
        // Remove allocations linked to this member to maintain referential integrity
        List<WBSAllocation> allocations = allocationRepository.findByMember_Id(member.getId());
        if (allocations != null && !allocations.isEmpty()) {
            allocationRepository.deleteAll(allocations);
        }
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public Page<Project> listProjectsForUser(String email, Pageable pageable) {
        UUID userId = null;
        try {
            org.springframework.http.ResponseEntity<java.util.Map<String, Object>> resp = userClient.getByEmail(email);
            if (resp != null && resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object v = resp.getBody().get("id");
                if (v != null) {
                    // value expected as String UUID
                    userId = UUID.fromString(v.toString());
                }
            }
        } catch (Exception ignored) {}

        if (userId == null) {
            return Page.empty();
        }
        // For regular users, return only projects they are a member of
        return projectRepository.findByMembers_EmployeeId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Project> listProjectsForAdmin(Pageable pageable) {
        // Determine current user email from JWT
        String email = getCurrentUserEmail();
        if (email == null || email.isBlank()) {
            return Page.empty();
        }

        // Call user-service to fetch user details (including b2bUnitId) by email
        UUID b2bUnitId = null;
        try {
            org.springframework.http.ResponseEntity<java.util.Map<String, Object>> resp = userClient.getByEmail(email);
            if (resp != null && resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object v = resp.getBody().get("b2bUnitId");
                if (v != null) {
                    // value expected as String UUID
                    b2bUnitId = UUID.fromString(v.toString());
                }
            }
        } catch (Exception ignored) {}

        if (b2bUnitId == null) {
            return Page.empty();
        }
        return projectRepository.findByB2bUnitId(b2bUnitId, pageable);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.isBlank()) return email;
                // fall back to subject if email claim not present
                String sub = jwt.getClaimAsString("sub");
                if (sub != null && !sub.isBlank()) return sub;
            }
            // fallback to name
            if (authentication.getName() != null && !authentication.getName().isBlank()) {
                return authentication.getName();
            }
        }
        return null;
    }
    
    // Keep existing methods for backward compatibility
    @Deprecated
    @Transactional(readOnly = true)
    public Page<Project> listProjectsByMember(UUID employeeId, Pageable pageable) {
        return projectRepository.findByMembers_EmployeeId(employeeId, pageable);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public Page<Project> listProjectsByClient(UUID clientId, Pageable pageable) {
        return projectRepository.findByClient_Id(clientId, pageable);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public Page<Project> listProjectsByB2bUnit(UUID b2bUnitId, Pageable pageable) {
        return projectRepository.findByB2bUnitId(b2bUnitId, pageable);
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
