package com.skillrat.user.organisation.repo;

import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.B2BUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface B2BUnitRepository extends JpaRepository<B2BUnit, UUID> {
    List<B2BUnit> findByStatus(B2BUnitStatus status);
    Page<B2BUnit> findByStatus(B2BUnitStatus status, Pageable pageable);
    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);
}
