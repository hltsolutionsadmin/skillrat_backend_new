package com.skillrat.user.organisation.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.User;
import com.skillrat.user.organisation.domain.Address;
import com.skillrat.user.organisation.domain.B2BGroup;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.B2BUnitStatus;
import com.skillrat.user.organisation.repo.B2BUnitRepository;
import com.skillrat.user.organisation.web.dto.OnboardRequest;
import com.skillrat.user.organisation.web.mapper.OnboardingMapper;
import com.skillrat.user.repo.UserRepository;
import com.skillrat.user.service.UserService;

@Service
public class B2BUnitService {

    private static final Logger log = LoggerFactory.getLogger(B2BUnitService.class);

    private final B2BUnitRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final B2BGroupService groupService;

    public B2BUnitService(B2BUnitRepository repository, 
    		UserRepository userRepository, UserService userService, B2BGroupService groupService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userService = userService;
		this.groupService = groupService;
    }

    @Transactional
    public B2BUnit selfSignup(OnboardRequest request) {
    	String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    	B2BUnit unit=createB2BUnit(request,tenantId, true);
        log.info("B2BUnit self signup created id={}, name={}, tenantId= {}", unit.getId(), unit.getName(), tenantId);

        // Assign the current user as BUSINESS_ADMIN for this unit directly via UserService
        String callerEmail = getCurrentUserEmail();
        try {
            userService.assignBusinessAdmin(unit.getId(), callerEmail);
            log.info("Assigned BUSINESS_ADMIN via service for unitId={}, email={}", unit.getId(), callerEmail);
        } catch (Exception ex) {
            log.warn("Failed to assign business-admin via service unitId={}, email={}", unit.getId(), callerEmail, ex);
        }
        return unit;
    }

    @Transactional
    public Optional<B2BUnit> approve(@NonNull UUID id, String approver) {
        return repository.findById(id).map(unit -> {
            unit.setStatus(B2BUnitStatus.APPROVED);
            User approverUser = resolveUserByEmail(approver);
            if (approverUser == null) {
                approverUser = getCurrentUser();
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
    public Optional<B2BUnit> findById(@NonNull UUID id) {
        return repository.findById(id);
    }

    public Page<B2BUnit> listPending(Pageable pageable) {
        return repository.findByStatus(B2BUnitStatus.PENDING_APPROVAL, pageable);
    }

    private User getCurrentUser() {
    	String email=getCurrentUserEmail();
    	if (Objects.nonNull(email)) {
            return userRepository.findByEmailIgnoreCase(email).orElse(null);
        }
    	return null;
    }
    
    private String getCurrentUserEmail() {
    	try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                return jwtAuth.getToken().getClaimAsString("email");
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
    
    private B2BUnit createB2BUnit(OnboardRequest request,String tenantId, boolean selfOnboard) {
        if (repository.existsByNameIgnoreCaseAndTenantId(request.getName(), tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(request.getName());
        unit.setType(request.getType());
        unit.setContactEmail(request.getContactEmail());
        unit.setContactPhone(request.getContactPhone());
        unit.setWebsite(request.getWebsite());
        Address address = OnboardingMapper.toEntity(request.getAddress());
        if (Objects.nonNull(address)) {
            address.setTenantId(tenantId);
            unit.setAddress(address);
        }
        String code = request.getGroupName();
        if (Objects.nonNull(code)) {
            B2BGroup group = groupService.findOrCreate(code, tenantId);
            unit.setGroup(group);
        }
        unit.setTenantId(tenantId);
        unit.setOnboardedBy(getCurrentUser());
        if(selfOnboard) {
        	unit.setStatus(B2BUnitStatus.PENDING_APPROVAL);
		} else {
			unit.setStatus(B2BUnitStatus.APPROVED);
			unit.setApprovedBy(getCurrentUser());
			unit.setApprovedAt(Instant.now());
		}
        return repository.save(unit);
    }
}
