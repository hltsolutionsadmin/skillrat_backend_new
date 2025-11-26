package com.skillrat.wallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://localhost:8080/oauth/check_token}")
    private String introspectionUri;
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id:gateway}")
    private String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret:gateway-secret}")
    private String clientSecret;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector introspector() {
        NimbusOpaqueTokenIntrospector delegate = new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
        return token -> {
            OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);
            Collection<GrantedAuthority> authorities = new ArrayList<>(principal.getAuthorities());
            List<String> roles = principal.getAttribute("roles");
            if (roles != null) {
                for (String r : roles) {
                    if (r != null && !r.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                    }
                }
            }
            return new OAuth2IntrospectionAuthenticatedPrincipal(principal.getName(), principal.getAttributes(), authorities);
        };
    }
}
