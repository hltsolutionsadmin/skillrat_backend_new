package com.skillrat.organisation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://auth-service:8080/oauth/check_token}")
    private String introspectionUri;
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id:gateway}")
    private String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret:gateway-secret}")
    private String clientSecret;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.opaqueToken(opaque ->
                        opaque.introspectionUri(introspectionUri)
                                .introspectionClientCredentials(clientId, clientSecret)))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
