package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.UserGroup;
import com.skillrat.project.domain.UserGroupMember;
import com.skillrat.project.domain.UserGroupRole;
import com.skillrat.project.repo.UserGroupMemberRepository;
import com.skillrat.project.repo.UserGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserGroupService {

    private final UserGroupRepository groupRepo;
    private final UserGroupMemberRepository memberRepo;

    public UserGroupService(UserGroupRepository groupRepo, UserGroupMemberRepository memberRepo) {
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
    }

    private String tenant() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }

    @Transactional
    public UserGroup create(String name, String description, UUID b2bUnitId, UUID projectId, UUID leadId) {
        UserGroup g = new UserGroup();
        g.setName(name);
        g.setDescription(description);
        g.setB2bUnitId(b2bUnitId);
        g.setProjectId(projectId);
        g.setLeadId(leadId);
        g.setTenantId(tenant());
        return groupRepo.save(g);
    }

    @Transactional(readOnly = true)
    public Optional<UserGroup> get(UUID id) {
        return groupRepo.findByIdAndTenantId(id, tenant());
    }

    @Transactional(readOnly = true)
    public List<UserGroup> listByProject(UUID projectId) {
        return groupRepo.findByProjectIdAndTenantId(projectId, tenant());
    }

    @Transactional(readOnly = true)
    public List<UserGroup> listByB2BUnit(UUID b2bUnitId) {
        return groupRepo.findByB2bUnitIdAndTenantId(b2bUnitId, tenant());
    }

    @Transactional(readOnly = true)
    public List<UserGroup> listByLead(UUID leadId) {
        return groupRepo.findByLeadIdAndTenantId(leadId, tenant());
    }

    @Transactional(readOnly = true)
    public List<UserGroup> listByUser(UUID userId) {
        return memberRepo.findByUserIdAndTenantId(userId, tenant())
                .stream()
                .map(UserGroupMember::getGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserGroupMember addMember(UUID groupId, UUID userId, UserGroupRole role) {
        UserGroup group = groupRepo.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        if (memberRepo.existsByGroup_IdAndUserIdAndActiveTrue(groupId, userId)) {
            return memberRepo.findByGroup_Id(groupId).stream()
                    .filter(m -> m.getUserId().equals(userId) && m.isActive())
                    .findFirst().orElseThrow();
        }
        UserGroupMember m = new UserGroupMember();
        m.setGroup(group);
        m.setUserId(userId);
        m.setRole(role == null ? UserGroupRole.MEMBER : role);
        m.setActive(true);
        m.setTenantId(tenant());
        return memberRepo.save(m);
    }

    @Transactional
    public void removeMember(UUID groupId, UUID userId) {
        memberRepo.deleteByGroup_IdAndUserId(groupId, userId);
    }

    @Transactional(readOnly = true)
    public List<UserGroupMember> listMembers(UUID groupId) {
        return memberRepo.findByGroup_Id(groupId);
    }
}
