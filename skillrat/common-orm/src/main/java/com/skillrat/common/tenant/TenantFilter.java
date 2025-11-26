package com.skillrat.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantFilter extends OncePerRequestFilter {
    public static final String TENANT_HEADER = "X-Skillrat-Tenant";
    private final String baseDomain;

    public TenantFilter(String baseDomain) {
        this.baseDomain = baseDomain;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenant = resolveTenant(request);
            if (tenant != null) {
                TenantContext.setTenantId(tenant);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        String header = request.getHeader(TENANT_HEADER);
        if (StringUtils.hasText(header)) return header;
        String host = request.getServerName();
        if (host != null && baseDomain != null && host.endsWith(baseDomain)) {
            String sub = host.substring(0, host.length() - baseDomain.length());
            if (sub.endsWith(".")) sub = sub.substring(0, sub.length() - 1);
            if (StringUtils.hasText(sub)) return sub;
        }
        return null;
    }
}
