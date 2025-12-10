package com.skillrat.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class PublicRoutesBypassFilter implements GlobalFilter {

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private static final String[] PUBLIC_PATTERNS = new String[] {
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // user-service public
            "/api/users/signup",
            "/api/users/login",
            "/api/users/password/reset",
            "/api/users/password/setup",
            // placement-service public
            "/api/openings/public/**",
            "/api/openings/*/apply",
            // project-service public
            "/api/projects/*/incidents"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth)) {
            return chain.filter(exchange);
        }
        // If path is public, strip Authorization so downstream filters skip introspection
        for (String pattern : PUBLIC_PATTERNS) {
            if (matcher.match(pattern, path)) {
                ServerHttpRequest mutated = request.mutate()
                        .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                        .build();
                return chain.filter(exchange.mutate().request(mutated).build());
            }
        }
        return chain.filter(exchange);
    }
}
