package com.skillrat.organisation.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.organisation.domain.B2BUnit;
import com.skillrat.organisation.domain.B2BUnitStatus;
import com.skillrat.organisation.domain.B2BUnitType;
import com.skillrat.organisation.repo.B2BUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class B2BUnitService {

    private final B2BUnitRepository repository;

    public B2BUnitService(B2BUnitRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public B2BUnit selfSignup(String name, B2BUnitType type, String email, String phone, String website, String address) {
        String tenantId = TenantContext.getTenantId();
        if (repository.existsByNameIgnoreCaseAndTenantId(name, tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(name);
        unit.setType(type);
        unit.setContactEmail(email);
        unit.setContactPhone(phone);
        unit.setWebsite(website);
        unit.setAddress(address);
        unit.setOnboardedBy("SELF");
        unit.setStatus(B2BUnitStatus.PENDING_APPROVAL);
        return repository.save(unit);
    }

    @Transactional
    public B2BUnit adminOnboard(String name, B2BUnitType type, String email, String phone, String website, String address, String approver) {
        String tenantId = TenantContext.getTenantId();
        if (repository.existsByNameIgnoreCaseAndTenantId(name, tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(name);
        unit.setType(type);
        unit.setContactEmail(email);
        unit.setContactPhone(phone);
        unit.setWebsite(website);
        unit.setAddress(address);
        unit.setOnboardedBy("ADMIN");
        unit.setStatus(B2BUnitStatus.APPROVED);
        unit.setApprovedBy(approver != null ? approver : "skillrat-admin");
        unit.setApprovedAt(Instant.now());
        return repository.save(unit);
    }

    @Transactional
    public Optional<B2BUnit> approve(UUID id, String approver) {
        return repository.findById(id).map(unit -> {
            unit.setStatus(B2BUnitStatus.APPROVED);
            unit.setApprovedBy(approver);
            unit.setApprovedAt(Instant.now());
            return repository.save(unit);
        });
    }

    public List<B2BUnit> listPending() {
        return repository.findByStatus(B2BUnitStatus.PENDING_APPROVAL);
    }
}
