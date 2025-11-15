package com.skillrat.user.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(String firstName, String lastName, String email, String mobile, String rawPassword) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile).ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
        }
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(email.toLowerCase());
        u.setEmail(email.toLowerCase());
        u.setMobile(mobile);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setActive(true);
        u.setTenantId(tenantId);
        User saved = userRepository.save(u);
        log.info("User signup successful id={}, email={}, tenantId={}", saved.getId(), saved.getEmail(), tenantId);
        return saved;
    }

    @Transactional
    public User adminCreateUser(UUID b2bUnitId, String firstName, String lastName, String email, String mobile, List<UUID> roleIds) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile).ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
        }
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(email.toLowerCase());
        u.setEmail(email.toLowerCase());
        u.setMobile(mobile);
        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setActive(true);
        u.setTenantId(tenantId);
        u.setB2bUnitId(b2bUnitId);
        u.setPasswordNeedsReset(true);
        u.setPasswordSetupToken(UUID.randomUUID().toString());
        u.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            u.setRoles(roles);
        }
        return userRepository.save(u);
    }

    @Transactional
    public User adminUpdateUser(UUID id, String firstName, String lastName, String mobile, Boolean active, List<UUID> roleIds) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (firstName != null && !firstName.isBlank()) u.setFirstName(firstName.trim());
        if (lastName != null && !lastName.isBlank()) u.setLastName(lastName.trim());
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile)
                    .filter(x -> !x.getId().equals(id))
                    .ifPresent(x -> { throw new IllegalArgumentException("Mobile already in use"); });
            u.setMobile(mobile);
        }
        if (active != null) u.setActive(active);
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            u.setRoles(roles);
        }
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String emailOrMobile, String rawPassword) {
        Optional<User> byEmail = userRepository.findByEmailIgnoreCase(emailOrMobile);
        Optional<User> byMobile = byEmail.isPresent() ? byEmail : userRepository.findByMobile(emailOrMobile);
        Optional<User> result = byMobile.filter(User::isActive)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
        if (result.isEmpty()) {
            log.warn("User authentication failed for identifier={}", emailOrMobile);
        } else {
            log.info("User authentication successful id={}, email={}", result.get().getId(), result.get().getEmail());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String q, String role, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        String roleName = (role == null || role.isBlank() || "All".equalsIgnoreCase(role)) ? null : role.trim();
        return userRepository.search(query, roleName, pageable);
    }

    @Transactional
    public User createBusinessAdmin(UUID b2bUnitId, String firstName, String lastName, String email, String mobile) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        User admin = new User();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setUsername(email.toLowerCase());
        admin.setEmail(email.toLowerCase());
        admin.setMobile(mobile);
        admin.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setActive(true);
        admin.setTenantId(tenantId);
        admin.setB2bUnitId(b2bUnitId);
        admin.setPasswordNeedsReset(true);
        admin.setPasswordSetupToken(UUID.randomUUID().toString());
        admin.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));

        // Ensure ADMIN role exists for this business
        Role adminRole = roleRepository.findByNameAndB2bUnitId("ADMIN", b2bUnitId)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ADMIN");
                    r.setB2bUnitId(b2bUnitId);
                    r.setTenantId(tenantId);
                    return roleRepository.save(r);
                });
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);
        User saved = userRepository.save(admin);
        log.info("Business admin user created id={}, email={}, b2bUnitId={}, tenantId={}",
                saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
        return saved;
    }

    @Transactional
    public User assignBusinessAdmin(UUID b2bUnitId, String email) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email"));

        // Ensure ADMIN role exists for this business (scoped role)
        Role adminRole = roleRepository.findByNameAndB2bUnitId("ADMIN", b2bUnitId)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("BUSINESS_ADMIN");
                    r.setB2bUnitId(b2bUnitId);
                    r.setTenantId(tenantId);
                    return roleRepository.save(r);
                });

        Set<Role> roles = new java.util.HashSet<>(Optional.ofNullable(user.getRoles()).orElseGet(java.util.HashSet::new));
        roles.add(adminRole);
        user.setRoles(roles);
        user.setB2bUnitId(b2bUnitId);
        User saved = userRepository.save(user);
        log.info("Assigned BUSINESS_ADMIN role to user id={}, email={}, b2bUnitId={}, tenantId={}",
                saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
        return saved;
    }

    @Transactional
    public Employee inviteEmployee(UUID b2bUnitId, String firstName, String lastName, String email, String mobile, List<UUID> roleIds) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        Employee emp = new Employee();
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setUsername(email.toLowerCase());
        emp.setEmail(email.toLowerCase());
        emp.setMobile(mobile);
        emp.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0,8));
        emp.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        emp.setActive(true);
        emp.setTenantId(tenantId);
        emp.setB2bUnitId(b2bUnitId);
        emp.setPasswordNeedsReset(true);
        emp.setPasswordSetupToken(UUID.randomUUID().toString());
        emp.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            emp.setRoles(roles);
        }
        // Populate createdBy/updatedBy from current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String actor = auth.getName();
            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String emailClaim = jwt.getClaimAsString("email");
                if (emailClaim != null && !emailClaim.isBlank()) {
                    actor = emailClaim;
                }
            }
            if (actor != null && !actor.isBlank()) {
                emp.setCreatedBy(actor);
                emp.setUpdatedBy(actor);
            }
        }
        Employee saved = userRepository.save(emp);
        log.info("Employee invited id={}, email={}, b2bUnitId={}, tenantId={}",
                saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
        return saved;
    }

    @Transactional
    public boolean setupPassword(String token, String newPassword) {
        Optional<User> uOpt = userRepository.findByPasswordSetupToken(token);
        if (uOpt.isEmpty()) return false;
        User u = uOpt.get();
        if (u.getPasswordSetupTokenExpires() == null || u.getPasswordSetupTokenExpires().isBefore(Instant.now())) {
            return false;
        }
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        u.setPasswordNeedsReset(false);
        u.setPasswordSetupToken(null);
        u.setPasswordSetupTokenExpires(null);
        userRepository.save(u);
        log.info("Password setup completed for user id={}, email={}", u.getId(), u.getEmail());
        return true;
    }
}
