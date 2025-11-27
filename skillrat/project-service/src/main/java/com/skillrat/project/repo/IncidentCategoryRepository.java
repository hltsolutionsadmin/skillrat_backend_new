package com.skillrat.project.repo;

import com.skillrat.project.domain.IncidentCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncidentCategoryRepository extends JpaRepository<IncidentCategoryEntity, UUID> {
    boolean existsByTenantIdAndOrganisationIdAndCodeIgnoreCase(String tenantId, UUID organisationId, String code);
    boolean existsByTenantIdAndOrganisationIdAndCodeIgnoreCaseAndIdNot(String tenantId, UUID organisationId, String code, UUID id);

    Page<IncidentCategoryEntity> findAllByTenantIdAndOrganisationId(String tenantId, UUID organisationId, Pageable pageable);

    Optional<IncidentCategoryEntity> findByIdAndTenantId(UUID id, String tenantId);
    Optional<IncidentCategoryEntity> findByIdAndTenantIdAndOrganisationId(UUID id, String tenantId, UUID organisationId);
}
