package com.skillrat.user.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(String firstName, String lastName, String email, String mobile, String rawPassword) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        // Basic uniqueness checks
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
}
