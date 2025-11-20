package com.skillrat.user.repo;

import com.skillrat.user.domain.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    List<UserSkill> findByUserIdAndTenantId(UUID userId, String tenantId);
    
    @Query("SELECT s FROM UserSkill s WHERE s.userId = :userId AND (s.tenantId = :tenantId OR s.tenantId IS NULL)")
    List<UserSkill> findByUserIdAndTenantIdOrNull(@Param("userId") UUID userId, @Param("tenantId") String tenantId);
}
