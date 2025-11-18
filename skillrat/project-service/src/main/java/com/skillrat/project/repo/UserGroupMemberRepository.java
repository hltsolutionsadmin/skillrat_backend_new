package com.skillrat.project.repo;

import com.skillrat.project.domain.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, UUID> {
    boolean existsByGroup_IdAndUserIdAndActiveTrue(UUID groupId, UUID userId);
    List<UserGroupMember> findByGroup_Id(UUID groupId);
    List<UserGroupMember> findByUserIdAndTenantId(UUID userId, String tenantId);
    void deleteByGroup_IdAndUserId(UUID groupId, UUID userId);
}
