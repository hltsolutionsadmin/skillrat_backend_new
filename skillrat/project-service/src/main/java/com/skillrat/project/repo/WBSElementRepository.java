package com.skillrat.project.repo;

import com.skillrat.project.domain.WBSElement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WBSElementRepository extends JpaRepository<WBSElement, UUID> {
    Optional<WBSElement> findByCodeAndTenantId(String code, String tenantId);
    List<WBSElement> findByProject_Id(UUID projectId);
    Page<WBSElement> findByProject_Id(UUID projectId, Pageable pageable);

    Optional<WBSElement> findByCode(String code);

    Page<WBSElement> findByB2bUnitId(UUID b2bUnitId, PageRequest pageRequest);
}
