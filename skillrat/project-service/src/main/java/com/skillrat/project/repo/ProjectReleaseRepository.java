package com.skillrat.project.repo;

import com.skillrat.project.domain.Project;
import com.skillrat.project.domain.ProjectRelease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectReleaseRepository extends JpaRepository<ProjectRelease, UUID> {
    
    Page<ProjectRelease> findByProjectId(UUID projectId, Pageable pageable);
    
    List<ProjectRelease> findByProjectIdOrderByReleaseDateDesc(UUID projectId);
    
    boolean existsByProjectIdAndVersion(UUID projectId, String version);
    
    Page<ProjectRelease> findByProjectIdAndStatus(UUID projectId, ProjectRelease.ReleaseStatus status, Pageable pageable);
    
    List<ProjectRelease> findByProjectIdAndStatusOrderByReleaseDateDesc(UUID projectId, ProjectRelease.ReleaseStatus status);
    
    Optional<ProjectRelease> findByIdAndProjectId(UUID id, UUID projectId);

    List<ProjectRelease> findByProjectId(UUID projectId);

}
