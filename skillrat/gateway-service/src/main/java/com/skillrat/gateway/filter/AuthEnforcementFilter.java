package com.skillrat.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthEnforcementFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.toLowerCase().startsWith("bearer ")) {
            // No bearer token: allow through (public or non-authenticated route)
            return chain.filter(exchange);
        }
        String claims = request.getHeaders().getFirst("X-Principal-Claims");
        boolean active = (claims != null && claims.contains("\"active\":true"));
        if (!active) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // Token active: strip Authorization before forwarding
        ServerHttpRequest mutated = request.mutate()
                .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }
}
