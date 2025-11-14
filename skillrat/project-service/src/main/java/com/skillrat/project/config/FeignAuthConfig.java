package com.skillrat.project.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth instanceof BearerTokenAuthentication bta) {
                    String token = bta.getToken().getTokenValue();
                    if (token != null && !token.isBlank()) {
                        template.header("Authorization", "Bearer " + token);
                    }
                }
            }
        };
    }
}
