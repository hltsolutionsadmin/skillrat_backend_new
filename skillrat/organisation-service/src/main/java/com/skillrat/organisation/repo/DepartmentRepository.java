package com.skillrat.organisation.repo;

import com.skillrat.organisation.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    Optional<Department> findByName(String name);
    
    Page<Department> findByB2bUnitId(UUID b2bUnitId, Pageable pageable);
    
    Page<Department> findByNameContainingIgnoreCaseAndB2bUnitId(String name, UUID b2bUnitId, Pageable pageable);
    
    Page<Department> findByActiveAndB2bUnitId(boolean active, UUID b2bUnitId, Pageable pageable);
}
