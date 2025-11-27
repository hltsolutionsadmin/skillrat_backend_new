package com.skillrat.user.organisation.repo;

import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.B2BUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface B2BUnitRepository extends JpaRepository<B2BUnit, UUID> {
    List<B2BUnit> findByStatus(B2BUnitStatus status);
    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);
}
