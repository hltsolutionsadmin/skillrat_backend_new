package com.skillrat.user.organisation.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.B2BUnitStatus;
import com.skillrat.user.organisation.domain.B2BUnitType;
import com.skillrat.user.organisation.domain.Address;
import com.skillrat.user.organisation.domain.B2BGroup;
import com.skillrat.user.organisation.repo.B2BUnitRepository;
import com.skillrat.user.organisation.repo.B2BGroupRepository;
import com.skillrat.user.organisation.web.dto.AdminOnboardRequest;
import com.skillrat.user.organisation.web.mapper.OnboardingMapper;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.UserRepository;
import com.skillrat.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class B2BUnitService {

    private static final Logger log = LoggerFactory.getLogger(B2BUnitService.class);

    private final B2BUnitRepository repository;
    private final B2BGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public B2BUnitService(B2BUnitRepository repository, B2BGroupRepository groupRepository, UserRepository userRepository, UserService userService) {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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
        unit.setOnboardedBy(resolveCurrentUser());
        unit.setStatus(B2BUnitStatus.PENDING_APPROVAL);
        unit = repository.save(unit);
        log.info("B2BUnit self signup created id={}, name={}, tenantId={}", unit.getId(), unit.getName(), tenantId);

        // Assign the current user as BUSINESS_ADMIN for this unit directly via UserService
        String callerEmail = email;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String sub = jwtAuth.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                callerEmail = sub;
            }
        }
        try {
            userService.assignBusinessAdmin(unit.getId(), callerEmail);
            log.info("Assigned BUSINESS_ADMIN via service for unitId={}, email={}", unit.getId(), callerEmail);
        } catch (Exception ex) {
            log.warn("Failed to assign business-admin via service unitId={}, email={}", unit.getId(), callerEmail, ex);
        }
        return unit;
    }

    @Transactional
    public B2BUnit adminOnboard(AdminOnboardRequest req) {
        String tenantId = java.util.Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (repository.existsByNameIgnoreCaseAndTenantId(req.getName(), tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(req.getName());
        unit.setType(req.getType());
        unit.setContactEmail(req.getContactEmail());
        unit.setContactPhone(req.getContactPhone());
        unit.setWebsite(req.getWebsite());
        Address address = OnboardingMapper.toEntity(req.getAddress());
        if (address != null) {
            address.setTenantId(tenantId);
            unit.setAddress(address);
        }
        String groupName = req.getGroupName();
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
        unit.setOnboardedBy(resolveCurrentUser());
        unit.setStatus(B2BUnitStatus.APPROVED);
        User approverUser = resolveUserByEmail(req.getApprover());
        if (approverUser == null) {
            approverUser = resolveCurrentUser();
        }
        unit.setApprovedBy(approverUser);
        unit.setApprovedAt(Instant.now());
        unit = repository.save(unit);
        log.info("B2BUnit admin onboarded id={}, name={}, tenantId={}, approvedBy={}", unit.getId(), unit.getName(), tenantId, unit.getApprovedBy());

        String adminEmail = req.getAdminEmail();
        if (adminEmail != null && !adminEmail.isBlank()) {
            try {
                userService.createBusinessAdmin(unit.getId(), req.getAdminFirstName(), req.getAdminLastName(), adminEmail, req.getAdminMobile());
            } catch (Exception ex) {
                // Intentionally not failing org onboarding; admin creation can be retried separately
                log.warn("Failed to create admin user for unitId={}, adminEmail={}", unit.getId(), adminEmail, ex);
            }
        }
        return unit;
    }

    @Transactional
    public Optional<B2BUnit> approve(UUID id, String approver) {
        return repository.findById(id).map(unit -> {
            unit.setStatus(B2BUnitStatus.APPROVED);
            User approverUser = resolveUserByEmail(approver);
            if (approverUser == null) {
                approverUser = resolveCurrentUser();
            }
            unit.setApprovedBy(approverUser);
            unit.setApprovedAt(Instant.now());
            B2BUnit saved = repository.save(unit);
            log.info("B2BUnit approved id={}, name={}, approverUserId={}", saved.getId(), saved.getName(),
                    saved.getApprovedBy() != null ? saved.getApprovedBy().getId() : null);
            return saved;
        });
    }

    @Transactional(readOnly = true)
    public Optional<B2BUnit> findById(UUID id) {
        return repository.findById(id);
    }

    public List<B2BUnit> listPending() {
        return repository.findByStatus(B2BUnitStatus.PENDING_APPROVAL);
    }

    private User resolveCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String email = jwtAuth.getToken().getClaimAsString("email");
                if (email == null || email.isBlank()) {
                    email = jwtAuth.getToken().getSubject();
                }
                if (email != null && !email.isBlank()) {
                    return userRepository.findByEmailIgnoreCase(email).orElse(null);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private User resolveUserByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            return userRepository.findByEmailIgnoreCase(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
