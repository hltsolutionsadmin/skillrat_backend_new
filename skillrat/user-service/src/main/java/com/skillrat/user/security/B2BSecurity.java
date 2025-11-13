package com.skillrat.user.security;

import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component("b2bSecurity")
public class B2BSecurity {

    private final UserRepository userRepository;

    public B2BSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasBusinessOrHrAdmin(UUID b2bUnitId) {
        if (b2bUnitId == null) return false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken token)) {
            return false;
        }
        String email = token.getToken().getSubject();
        if (email == null || email.isBlank()) return false;
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) return false;
        Set<Role> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) return false;
        return roles.stream()
                .anyMatch(r ->
                    Objects.equals(b2bUnitId, r.getB2bUnitId()) &&
                    ("BUSINESS_ADMIN".equalsIgnoreCase(r.getName()) ||
                     "HR_ADMIN".equalsIgnoreCase(r.getName()))
                );
    }
}
