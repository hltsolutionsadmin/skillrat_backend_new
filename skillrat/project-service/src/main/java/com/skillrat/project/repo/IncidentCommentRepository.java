package com.skillrat.project.repo;

import com.skillrat.project.domain.IncidentComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, UUID> {
    Page<IncidentComment> findByIncident_Id(UUID incidentId, Pageable pageable);
}
