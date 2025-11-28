package com.skillrat.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

@Configuration
public class ProjectAuditingConfig {

    @Bean(name = "projectAuditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }
            String userId = null;
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String sub = jwtAuth.getToken().getSubject();
                if (sub != null && !sub.isBlank()) {
                    userId = sub;
                }
            }
            if (userId == null || userId.isBlank()) {
                userId = auth.getName();
            }
            return Optional.ofNullable(userId != null && !userId.isBlank() ? userId : "system");
        };
    }
}
