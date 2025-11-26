package com.skillrat.placement.repo;

import com.skillrat.placement.domain.Opening;
import com.skillrat.placement.domain.OpeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpeningRepository extends JpaRepository<Opening, UUID> {
    List<Opening> findByB2bUnitId(UUID b2bUnitId);
    List<Opening> findByStatus(OpeningStatus status);
}
