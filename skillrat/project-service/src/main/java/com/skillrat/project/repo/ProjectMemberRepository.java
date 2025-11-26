package com.skillrat.project.repo;

import com.skillrat.project.domain.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProject_Id(UUID projectId);
    Optional<ProjectMember> findByProject_IdAndEmployeeId(UUID projectId, UUID employeeId);
    List<ProjectMember> findByEmployeeId(UUID employeeId);
    Page<ProjectMember> findByEmployeeId(UUID employeeId, Pageable pageable);
}
