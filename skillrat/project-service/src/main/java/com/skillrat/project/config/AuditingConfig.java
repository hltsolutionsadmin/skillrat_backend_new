package com.skillrat.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof Jwt jwt) {
                        String email = jwt.getClaimAsString("email");
                        if (email != null && !email.isBlank()) return Optional.of(email);
                        String sub = jwt.getClaimAsString("sub");
                        if (sub != null && !sub.isBlank()) return Optional.of(sub);
                    }
                    String name = authentication.getName();
                    if (name != null && !name.isBlank()) return Optional.of(name);
                }
            } catch (Exception ignored) {}
            return Optional.of("system");
        };
    }
}
