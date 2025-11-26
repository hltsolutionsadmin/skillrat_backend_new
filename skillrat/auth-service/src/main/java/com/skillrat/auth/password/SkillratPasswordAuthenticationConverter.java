package com.skillrat.auth.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class SkillratPasswordAuthenticationConverter implements AuthenticationConverter {
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:skillrat-password";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter("grant_type");
        if (!GRANT_TYPE.equals(grantType)) {
            return null;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Authentication client = SecurityContextHolder.getContext().getAuthentication();
        return new SkillratPasswordAuthenticationToken(client, username, password, client.getAuthorities(), null);
    }
}
