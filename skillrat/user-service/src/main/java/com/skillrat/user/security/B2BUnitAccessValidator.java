package com.skillrat.user.security;

import com.skillrat.user.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class B2BUnitAccessValidator {

    private final UserService userService;

    public B2BUnitAccessValidator(UserService userService) {
        this.userService = userService;
    }

    public void validateCurrentUserBelongsTo(UUID b2bUnitId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }

        String email = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            email = jwt.getClaimAsString("email");
        }
        if (email == null || email.isBlank()) {
            email = auth.getName();
        }

        String finalEmail = email;
        userService.findByEmail(finalEmail)
                .ifPresentOrElse(u -> {
                    UUID userB2b = u.getB2bUnitId();
                    if (userB2b == null || !userB2b.equals(b2bUnitId)) {
                        throw new AccessDeniedException("Forbidden for this business unit");
                    }
                }, () -> {
                    throw new AccessDeniedException("User context not found");
                });
    }
}
