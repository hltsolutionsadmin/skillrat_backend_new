package com.skillrat.project.repo;

import com.skillrat.project.domain.Incident;
import com.skillrat.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {
    Page<Incident> findByProject_Id(UUID projectId, Pageable pageable);

   Optional<Incident> findTopByProjectAndIncidentNumberStartingWithOrderByIncidentNumberDesc(Project project, String incidentNumberPrefix);

    Page<Incident> findByAssigneeId(UUID assigneeId, Pageable pageable);

    Page<Incident> findByReporterId(UUID reporterId, Pageable pageable);
}
