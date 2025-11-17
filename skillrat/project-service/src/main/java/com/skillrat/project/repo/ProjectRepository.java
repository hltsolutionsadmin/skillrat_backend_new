package com.skillrat.project.repo;

import com.skillrat.project.domain.Project;
import com.skillrat.project.domain.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByCodeAndTenantId(String code, String tenantId);
    Page<Project> findDistinctByMembers_EmployeeId(UUID employeeId, Pageable pageable);
    Page<Project> findByClient_Id(UUID clientId, Pageable pageable);
    Page<Project> findByB2bUnitId(UUID b2bUnitId, Pageable pageable);
}
