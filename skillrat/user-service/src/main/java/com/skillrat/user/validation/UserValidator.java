package com.skillrat.user.validation;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.skillrat.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateSignup(String email, String mobile, String rawPassword) {
        requireEmail(email);
        requirePassword(rawPassword);
        ensureEmailUnique(email);
        ensureMobileUniqueIfPresent(mobile);
    }

    public void validateAdminCreateUser(String email, String mobile) {
        requireEmail(email);
        ensureEmailUnique(email);
        ensureMobileUniqueIfPresent(mobile);
    }

    public void validateAdminUpdateUser(UUID id, String mobile) {
        if (id == null) throw new IllegalArgumentException("User ID is required");
        if (mobile != null && !mobile.isBlank()) {
            String trimmed = mobile.trim();
            userRepository.findByMobile(trimmed)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(x -> { throw new IllegalArgumentException("Mobile number already in use"); });
        }
    }

    public void validateInviteEmployee(String email) {
        requireEmail(email);
        ensureEmailUnique(email);
    }

    public void validatePasswordSetup(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        requirePassword(newPassword);
    }

    private void requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private void requirePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    private void ensureEmailUnique(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
    }

    private void ensureMobileUniqueIfPresent(String mobile) {
        if (mobile != null && !mobile.isBlank() && userRepository.existsByMobile(mobile.trim())) {
            throw new IllegalArgumentException("Mobile number already in use");
        }
    }
}
