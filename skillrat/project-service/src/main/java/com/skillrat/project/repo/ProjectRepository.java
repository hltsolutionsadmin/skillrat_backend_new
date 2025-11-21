package com.skillrat.project.repo;

import com.skillrat.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    // Find project by code and tenant ID
    Optional<Project> findByCodeAndTenantId(String code, String tenantId);
    
    // Find projects where user is a member
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.employeeId = :employeeId")
    Page<Project> findByMembers_EmployeeId(@Param("employeeId") UUID employeeId, Pageable pageable);
    
    // Find projects by client ID
    Page<Project> findByClient_Id(UUID clientId, Pageable pageable);
    
    // Find projects by B2B unit ID (organization)
    Page<Project> findByB2bUnitId(UUID b2bUnitId, Pageable pageable);
    
    // Find projects by B2B unit ID with optional name filter
    @Query("SELECT p FROM Project p WHERE p.b2bUnitId = :b2bUnitId AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Project> findByB2bUnitIdAndNameContainingIgnoreCase(
        @Param("b2bUnitId") UUID b2bUnitId,
        @Param("name") String name,
        Pageable pageable
    );

    Optional<Project>  findByCode(String code);
}
