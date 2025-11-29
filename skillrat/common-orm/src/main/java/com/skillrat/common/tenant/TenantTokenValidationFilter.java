package com.skillrat.common.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantTokenValidationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String tenant = TenantContext.getTenantId();
        String claimsJson = request.getHeader("X-Principal-Claims");
        if (StringUtils.hasText(tenant) && StringUtils.hasText(claimsJson)) {
            try {
                JsonNode node = objectMapper.readTree(claimsJson);
                JsonNode tokenTenant = node.get("tenant_id");
                if (tokenTenant != null && !tenant.equals(tokenTenant.asText())) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("Tenant mismatch");
                    return;
                }
            } catch (Exception e) {
                // If claims unparsable, proceed; resource server will still enforce auth
            }
        }
        filterChain.doFilter(request, response);
    }
}
