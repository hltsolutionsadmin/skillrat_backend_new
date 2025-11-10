package com.skillrat.user.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;
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

@Service
public class UserService {

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
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String emailOrMobile, String rawPassword) {
        Optional<User> byEmail = userRepository.findByEmailIgnoreCase(emailOrMobile);
        Optional<User> byMobile = byEmail.isPresent() ? byEmail : userRepository.findByMobile(emailOrMobile);
        return byMobile.filter(User::isActive)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
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
        return userRepository.save(admin);
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
        return userRepository.save(emp);
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
        return true;
    }
}
