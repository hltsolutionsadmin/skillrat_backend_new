package com.skillrat.project.repo;

import com.skillrat.project.domain.IncidentSubCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncidentSubCategoryRepository extends JpaRepository<IncidentSubCategoryEntity, UUID> {
    boolean existsByTenantIdAndCategory_IdAndCodeIgnoreCase(String tenantId, UUID categoryId, String code);
    boolean existsByTenantIdAndCategory_IdAndCodeIgnoreCaseAndIdNot(String tenantId, UUID categoryId, String code, UUID id);

    Page<IncidentSubCategoryEntity> findAllByTenantIdAndCategory_Id(String tenantId, UUID categoryId, Pageable pageable);


    Optional<IncidentSubCategoryEntity> findByIdAndTenantId(UUID id, String tenantId);
}
