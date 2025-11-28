package com.skillrat.project.service;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.client.UserClient;
import com.skillrat.project.domain.*;



import com.skillrat.project.repo.*;
import com.skillrat.project.web.request.CreateProjectRequest;
import com.skillrat.project.web.request.CreateWbsRequest;
import com.skillrat.project.web.request.UpdateProjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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
    public ProjectDTO createProject(CreateProjectRequest req, String createdBy) {
        String tenant = Optional.ofNullable(TenantContext.getTenantId())
                .filter(t -> !t.isBlank())
                .orElse("default");

        String code = req.getCode();
        if (code != null && !code.isBlank() && projectRepository.findByCodeAndTenantId(code, tenant).isPresent()) {
            throw new IllegalStateException("Project code already exists for tenant");
        }
        Optional<Project> dup = projectRepository.findByCode(code);
        if (dup.isPresent()) {
            throw new IllegalStateException("Project code already exists");
        }

        Project p = new Project();
        p.setName(req.getName());
        p.setCode(code);
        p.setB2bUnitId(req.getB2bUnitId());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setTenantId(tenant);
        p.setDescription(req.getDescription());
        p.setTaskManagement(req.isTaskManagement());
        p.setProjectManagement(req.isProjectManagement());

        if (ProjectType.SUPPORT.equals(req.getProjectType())) {
            p.setProjectType(req.getProjectType());
            if (ProjectSLAType.ENTERPRISE.equals(req.getProjectType())) {
                p.setStatus(ProjectSLAType.ENTERPRISE);
            }
        }
        if (req.getProjectStatus() != null) {
            p.setProjectStatus(req.getProjectStatus());
        }

        if (req.getClient() != null && req.getClient().getName() != null && !req.getClient().getName().isBlank()) {
            ProjectClient client = new ProjectClient();
            client.setName(req.getClient().getName());
            if (req.getClient().getPrimaryContactEmail() != null && !req.getClient().getPrimaryContactEmail().isBlank()) {
                client.setPrimaryContactEmail(req.getClient().getPrimaryContactEmail());
            }
            if (req.getClient().getSecondaryContactEmail() != null && !req.getClient().getSecondaryContactEmail().isBlank()) {
                client.setSecondaryContactEmail(req.getClient().getSecondaryContactEmail());
            }
            client.setTenantId(tenant);
            applyAuditFromCurrentUser(client);
            p.setClient(client);
        }

        p.setCreatedBy(createdBy);
        p.setUpdatedBy(createdBy);

        Project saved = projectRepository.save(p);
        return toDto(saved);
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
                                 ProjectType projectType, ProjectSLAType status, ProjectStatus projectStatus , boolean taskManagement, boolean projectManagement, String updatedBy) {
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
        p.setTaskManagement(taskManagement);
        p.setProjectManagement(projectManagement);
        if (ProjectType.SUPPORT.equals(projectType)) {
            p.setProjectType(projectType);
            if (ProjectSLAType.ENTERPRISE.equals(status)) {
                p.setStatus(ProjectSLAType.ENTERPRISE);
            }
        }
        if (projectStatus != null) {
            p.setProjectStatus(projectStatus);
        }

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

    // Wrapper that accepts request DTO and delegates to existing update method (no functionality change)
    @Transactional
    public ProjectDTO updateProject(UUID projectId, UpdateProjectRequest req, String updatedBy) {
        Project updated = updateProject(
                projectId,
                req.getName(),
                req.getCode(),
                req.getDescription(),
                req.getStartDate(),
                req.getEndDate(),
                req.getClient() != null ? req.getClient().getName() : null,
                req.getClient() != null ? req.getClient().getPrimaryContactEmail() : null,
                req.getClient() != null ? req.getClient().getSecondaryContactEmail() : null,
                req.getProjectType(),
                req.getStatus(),
                req.getProjectStatus(),
                Boolean.TRUE.equals(req.getTaskManagement()),
                Boolean.TRUE.equals(req.getProjectManagement()),
                updatedBy
        );
        return toDto(updated);
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

    // Wrapper that accepts request DTO for WBS creation
    @Transactional
    public WBSElement createWbs(UUID projectId, CreateWbsRequest req) {
        return createWbs(projectId, req.getName(), req.getCode(), req.getCategory(), req.getStartDate(), req.getEndDate());
    }

    @Transactional
    public WBSElement updateWbs(UUID wbsId, String name, String code, WBSCategory category, LocalDate start, LocalDate end, UUID projectId, boolean disabled) {
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
        if (disabled) wbs.setDisabled(disabled);
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

    // Wrapper that accepts request DTO for member upsert
    @Transactional
    public ProjectMember addOrUpdateMember(UUID projectId, com.skillrat.project.web.request.UpsertMemberRequest req) {
        return addOrUpdateMember(
                projectId,
                req.getEmployeeId(),
                req.getRole(),
                req.getReportingManagerId(),
                req.getStartDate(),
                req.getEndDate(),
                req.getActive() != null ? req.getActive() : true
        );
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

    // Wrapper that accepts request DTO for allocation
    @Transactional
    public WBSAllocation allocateMemberToWbs(UUID projectId, UUID userId, com.skillrat.project.web.request.AllocateRequest req) {
        return allocateMemberToWbs(projectId, userId, req.getWbsId(), req.getStartDate(), req.getEndDate());
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

    // Wrapper that accepts request DTO for WBS update
    @Transactional
    public WBSElement updateWbs(UUID wbsId, com.skillrat.project.web.request.UpdateWbsRequest req) {
        return updateWbs(wbsId, req.getName(), req.getCode(), req.getCategory(), req.getStartDate(), req.getEndDate(), req.getProjectId(), req.isDisabled());
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

    public ProjectDTO toDto(Project p) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setCode(p.getCode());
        dto.setDescription(p.getDescription());
        dto.setB2bUnitId(p.getB2bUnitId());
        dto.setHolidayCalendarId(p.getHolidayCalendarId());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setProjectType(p.getProjectType());
        if (p.getClient() != null) {
            ProjectClientDTO c = new ProjectClientDTO();
            c.setName(p.getClient().getName());
            c.setPrimaryContactEmail(p.getClient().getPrimaryContactEmail());
            c.setSecondaryContactEmail(p.getClient().getSecondaryContactEmail());
            dto.setClient(c);
        }
        dto.setStatus(p.getStatus());
        dto.setProjectStatus(p.getProjectStatus());
        dto.setTaskManagement(p.isTaskManagement());
        dto.setProjectManagement(p.isProjectManagement());
        dto.setCreatedBy(p.getCreatedBy());
        dto.setUpdatedBy(p.getUpdatedBy());
        // For creation, nested lists can be empty; populate elsewhere when needed
        dto.setWbsElements(java.util.Collections.emptyList());
        dto.setMembers(java.util.Collections.emptyList());
        return dto;
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectDTO(UUID id) {
        return toDto(getProject(id));
    }

    public WBSElementDTO toWbsDto(WBSElement w) {
        if (w == null) return null;
        WBSElementDTO dto = new WBSElementDTO();
        dto.setId(w.getId());
        dto.setProjectId(w.getProject() != null ? w.getProject().getId() : null);
        dto.setName(w.getName());
        dto.setCode(w.getCode());
        dto.setCategory(w.getCategory());
        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setDisabled(w.isDisabled());
        return dto;
    }

    public ProjectMemberDTO toMemberDto(ProjectMember m) {
        if (m == null) return null;
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setId(m.getId());
        dto.setProjectId(m.getProject() != null ? m.getProject().getId() : null);
        dto.setEmployeeId(m.getEmployeeId());
        dto.setRole(m.getRole());
        dto.setReportingManagerId(m.getReportingManagerId());
        dto.setStartDate(m.getStartDate());
        dto.setEndDate(m.getEndDate());
        dto.setActive(m.isActive());
        dto.setCreatedBy(m.getCreatedBy());
        dto.setUpdatedBy(m.getUpdatedBy());
        return dto;
    }

    public WBSAllocationDTO toAllocationDto(WBSAllocation a) {
        if (a == null) return null;
        WBSAllocationDTO dto = new WBSAllocationDTO();
        dto.setId(a.getId());
        dto.setMemberId(a.getMember() != null ? a.getMember().getId() : null);
        dto.setWbsElementId(a.getWbsElement() != null ? a.getWbsElement().getId() : null);
        dto.setStartDate(a.getStartDate());
        dto.setEndDate(a.getEndDate());
        dto.setActive(a.isActive());
        dto.setCreatedBy(a.getCreatedBy());
        dto.setUpdatedBy(a.getUpdatedBy());
        return dto;
    }
}
