package com.skillrat.user.repo;

import com.skillrat.user.domain.Designation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    boolean existsByName(String name);

    List<Designation> findAllByB2bUnitId(UUID b2bUnitId);
}

