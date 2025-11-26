package com.skillrat.auth.password;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public class SkillratPasswordAuthenticationToken extends AbstractAuthenticationToken {
    private final Authentication clientPrincipal;
    private final String username;
    private final String password;
    private final Map<String, Object> additionalParameters;

    public SkillratPasswordAuthenticationToken(Authentication clientPrincipal,
                                               String username,
                                               String password,
                                               Collection<? extends GrantedAuthority> authorities,
                                               Map<String, Object> additionalParameters) {
        super(authorities);
        this.clientPrincipal = clientPrincipal;
        this.username = username;
        this.password = password;
        this.additionalParameters = additionalParameters;
        setAuthenticated(false);
    }

    public Authentication getClientPrincipal() { return clientPrincipal; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Map<String, Object> getAdditionalParameters() { return additionalParameters; }

    @Override
    public Object getCredentials() { return password; }

    @Override
    public Object getPrincipal() { return username; }
}
