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
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ClaimsPropagationFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String claims = request.getHeaders().getFirst("X-Principal-Claims");
        if (!StringUtils.hasText(claims)) {
            return chain.filter(exchange);
        }
        // Very light parsing to extract common fields without introducing a JSON dependency here.
        String userId = extractJsonString(claims, "sub");
        if (!StringUtils.hasText(userId)) {
            userId = extractJsonString(claims, "user_id");
        }
        String username = extractJsonString(claims, "preferred_username");
        if (!StringUtils.hasText(username)) {
            username = extractJsonString(claims, "username");
        }
        String roles = extractJsonArrayAsCsv(claims, "roles");

        ServerHttpRequest.Builder builder = request.mutate();
        if (StringUtils.hasText(userId)) {
            builder.header("X-User-Id", userId);
        }
        if (StringUtils.hasText(username)) {
            builder.header("X-Username", username);
        }
        if (StringUtils.hasText(roles)) {
            builder.header("X-Roles", roles);
        }
        // Do not re-add Authorization; previous filters may have removed it intentionally
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private String extractJsonString(String json, String key) {
        // naive extraction: looks for "key":"value"
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            int start = m.end();
            int end = json.indexOf('"', start);
            if (end > start) {
                return json.substring(start, end);
            }
        }
        return null;
    }

    private String extractJsonArrayAsCsv(String json, String key) {
        // naive extraction: looks for "key":[ ... ] and returns comma-separated string of elements
        String pattern = "\"" + key + "\"\\s*:\\s*\\[";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            int start = m.end();
            int end = json.indexOf(']', start);
            if (end > start) {
                String arrayContent = json.substring(start, end);
                // remove quotes and whitespace around items
                String[] parts = arrayContent.split(",");
                java.util.List<String> cleaned = new java.util.ArrayList<>();
                for (String part : parts) {
                    if (part == null) continue;
                    String s = part.trim();
                    if (s.startsWith("\"")) s = s.substring(1);
                    if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
                    if (StringUtils.hasText(s)) cleaned.add(s);
                }
                return String.join(",", cleaned);
            }
        }
        return null;
    }
}
