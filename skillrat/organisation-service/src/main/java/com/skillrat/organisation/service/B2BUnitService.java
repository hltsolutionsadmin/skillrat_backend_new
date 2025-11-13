package com.skillrat.organisation.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.organisation.domain.B2BUnit;
import com.skillrat.organisation.domain.B2BUnitStatus;
import com.skillrat.organisation.domain.B2BUnitType;
import com.skillrat.organisation.domain.Address;
import com.skillrat.organisation.domain.B2BGroup;
import com.skillrat.organisation.repo.B2BUnitRepository;
import com.skillrat.organisation.repo.B2BGroupRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class B2BUnitService {

    private final B2BUnitRepository repository;
    private final B2BGroupRepository groupRepository;
    private final RestTemplate restTemplate;

    public B2BUnitService(B2BUnitRepository repository, B2BGroupRepository groupRepository, RestTemplate restTemplate) {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public B2BUnit selfSignup(String name, B2BUnitType type, String email, String phone, String website, Address address, String groupName) {
        String tenantId = java.util.Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (repository.existsByNameIgnoreCaseAndTenantId(name, tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(name);
        unit.setType(type);
        unit.setContactEmail(email);
        unit.setContactPhone(phone);
        unit.setWebsite(website);
        if (address != null) {
            address.setTenantId(tenantId);
            unit.setAddress(address);
        }
        if (groupName != null && !groupName.isBlank()) {
            B2BGroup group = groupRepository
                    .findByNameIgnoreCaseAndTenantId(groupName, tenantId)
                    .orElseGet(() -> {
                        B2BGroup g = new B2BGroup();
                        g.setName(groupName);
                        g.setTenantId(tenantId);
                        return groupRepository.save(g);
                    });
            unit.setGroup(group);
        }
        unit.setTenantId(tenantId);
        unit.setOnboardedBy("SELF");
        unit.setStatus(B2BUnitStatus.PENDING_APPROVAL);
        unit = repository.save(unit);

        try {
            // Derive onboarding user identity and token from current JWT
            String callerEmail = email;
            String bearerToken = null;
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                bearerToken = jwtAuth.getToken().getTokenValue();
                String sub = jwtAuth.getToken().getSubject();
                if (sub != null && !sub.isBlank()) {
                    callerEmail = sub;
                }
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Skillrat-Tenant", tenantId);
            if (bearerToken != null) {
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            }
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("b2bUnitId", unit.getId());
            payload.put("email", callerEmail);
            restTemplate.postForEntity("http://localhost:8081/api/users/internal/business-admin/assign", new HttpEntity<>(payload, headers), Void.class);
        } catch (Exception ignored) {
        	System.out.println(ignored);
        }
        return unit;
    }

    @Transactional
    public B2BUnit adminOnboard(String name, B2BUnitType type, String email, String phone, String website, Address address, String groupName, String approver,
                                String adminFirstName, String adminLastName, String adminEmail, String adminMobile) {
        String tenantId = java.util.Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (repository.existsByNameIgnoreCaseAndTenantId(name, tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(name);
        unit.setType(type);
        unit.setContactEmail(email);
        unit.setContactPhone(phone);
        unit.setWebsite(website);
        if (address != null) {
            address.setTenantId(tenantId);
            unit.setAddress(address);
        }
        if (groupName != null && !groupName.isBlank()) {
            B2BGroup group = groupRepository
                    .findByNameIgnoreCaseAndTenantId(groupName, tenantId)
                    .orElseGet(() -> {
                        B2BGroup g = new B2BGroup();
                        g.setName(groupName);
                        g.setTenantId(tenantId);
                        return groupRepository.save(g);
                    });
            unit.setGroup(group);
        }
        unit.setTenantId(tenantId);
        unit.setOnboardedBy("ADMIN");
        unit.setStatus(B2BUnitStatus.APPROVED);
        unit.setApprovedBy(approver != null ? approver : "skillrat-admin");
        unit.setApprovedAt(Instant.now());
        unit = repository.save(unit);

        if (adminEmail != null && !adminEmail.isBlank()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Skillrat-Tenant", tenantId);
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("b2bUnitId", unit.getId());
            payload.put("firstName", adminFirstName);
            payload.put("lastName", adminLastName);
            payload.put("email", adminEmail);
            payload.put("mobile", adminMobile);
            try {
                restTemplate.postForEntity("http://user-service:8080/api/users/internal/business-admin", new HttpEntity<>(payload, headers), Void.class);
            } catch (Exception ex) {
                // Intentionally not failing org onboarding; admin creation can be retried separately
            }
        }
        return unit;
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
