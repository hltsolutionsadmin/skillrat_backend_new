package com.skillrat.auth.config;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class TokenClaimsCustomizer {

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> oauth2TokenCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication principal = context.getPrincipal();
                Set<String> roles = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(a -> a != null && a.startsWith("ROLE_") ? a.substring(5) : a)
                        .collect(Collectors.toSet());
                String tenantId = TenantContext.getTenantId();
                context.getClaims().claim("tenant_id", tenantId);
                context.getClaims().claim("roles", roles);
                // Example userId placeholder from principal name; replace with real id once AuthUser is wired
                context.getClaims().claim("userId", principal.getName());
            }
        };
    }
}
