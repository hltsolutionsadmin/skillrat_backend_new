package com.skillrat.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientCredentialFilter extends OncePerRequestFilter {

    private final String clientId;
    private final String clientSecret;

    public ClientCredentialFilter(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (HttpMethod.POST.matches(request.getMethod()) && matchesIncidentCreate(request)) {
            if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain;charset=UTF-8");
                response.getOutputStream().write("Client credentials not configured".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String headerId = request.getHeader("X-Client-Id");
            String headerSecret = request.getHeader("X-Client-Secret");

            if (!StringUtils.hasText(headerId) || !StringUtils.hasText(headerSecret)
                    || !clientId.equals(headerId) || !clientSecret.equals(headerSecret)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain;charset=UTF-8");
                response.getOutputStream().write("Unauthorized".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean matchesIncidentCreate(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) return false;
        // Allow optional trailing slash and enforce exact pattern /api/projects/{projectId}/incidents
        return path.matches("^/api/projects/[^/]+/incidents/?$");
    }
}
