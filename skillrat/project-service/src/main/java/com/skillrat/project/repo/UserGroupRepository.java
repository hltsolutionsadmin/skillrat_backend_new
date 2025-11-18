package com.skillrat.project.repo;

import com.skillrat.project.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    Optional<UserGroup> findByIdAndTenantId(UUID id, String tenantId);
    List<UserGroup> findByProjectIdAndTenantId(UUID projectId, String tenantId);
    List<UserGroup> findByB2bUnitIdAndTenantId(UUID b2bUnitId, String tenantId);
    List<UserGroup> findByLeadIdAndTenantId(UUID leadId, String tenantId);
}
