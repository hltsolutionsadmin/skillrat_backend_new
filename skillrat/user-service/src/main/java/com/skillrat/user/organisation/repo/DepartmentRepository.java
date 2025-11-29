package com.skillrat.user.organisation.repo;

import com.skillrat.user.organisation.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    Optional<Department> findByName(String name);
    
    @Query("SELECT d FROM Department d JOIN d.b2bUnits b2b WHERE b2b.id = :b2bUnitId")
    Page<Department> findByB2bUnitId(@Param("b2bUnitId") UUID b2bUnitId, Pageable pageable);
    
    @Query("SELECT d FROM Department d JOIN d.b2bUnits b2b WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND b2b.id = :b2bUnitId")
    Page<Department> findByNameContainingIgnoreCaseAndB2bUnitId(@Param("name") String name, @Param("b2bUnitId") UUID b2bUnitId, Pageable pageable);
    
    @Query("SELECT d FROM Department d JOIN d.b2bUnits b2b WHERE d.active = :active AND b2b.id = :b2bUnitId")
    Page<Department> findByActiveAndB2bUnitId(@Param("active") boolean active, @Param("b2bUnitId") UUID b2bUnitId, Pageable pageable);

    Optional<Department> findByCode(String code);
}
