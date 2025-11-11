package com.skillrat.project.repo;

import com.skillrat.project.domain.WBSElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WBSElementRepository extends JpaRepository<WBSElement, UUID> {
    Optional<WBSElement> findByCodeAndTenantId(String code, String tenantId);
    List<WBSElement> findByProject_Id(UUID projectId);
}
