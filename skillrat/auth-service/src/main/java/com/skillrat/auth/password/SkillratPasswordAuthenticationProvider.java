package com.skillrat.auth.password;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SkillratPasswordAuthenticationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository clientRepository;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final RestTemplate restTemplate = new RestTemplate();

    public static final AuthorizationGrantType GRANT_TYPE = new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:skillrat-password");

    public SkillratPasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                  RegisteredClientRepository clientRepository,
                                                  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        this.authorizationService = authorizationService;
        this.clientRepository = clientRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof SkillratPasswordAuthenticationToken tokenRequest)) {
            return null;
        }
        // Extract client
        Authentication clientPrincipal = tokenRequest.getClientPrincipal();
        Object clientIdAttr = clientPrincipal.getName();
        RegisteredClient registeredClient = clientRepository.findByClientId(String.valueOf(clientIdAttr));
        if (registeredClient == null || !registeredClient.getAuthorizationGrantTypes().contains(GRANT_TYPE)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unauthorized_client", "Client not authorized for skillrat-password grant", null));
        }

        // Validate credentials against user-service
        String username = tokenRequest.getUsername();
        String password = tokenRequest.getPassword();
        if (username == null || password == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request", "username and password are required", null));
        }
        Map<String, String> body = new HashMap<>();
        body.put("emailOrMobile", username);
        body.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Propagate tenant header if present in SecurityContext? Not readily available; default to 'default'
        String tenant = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : "default";
        headers.add("X-Skillrat-Tenant", tenant);
        ResponseEntity<Map> resp = restTemplate.exchange("http://user-service:8080/api/users/login", HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("access_denied", "Invalid credentials", null));
        }
        Map<?,?> userInfo = resp.getBody();
        Object emailObj = userInfo != null ? userInfo.get("email") : null;
        String principalName = emailObj != null ? emailObj.toString() : username;

        java.util.List<?> rolesList = userInfo != null ? (java.util.List<?>) userInfo.get("roles") : java.util.List.of();
        java.util.List<SimpleGrantedAuthority> auths = new java.util.ArrayList<>();
        if (rolesList != null) {
            for (Object r : rolesList) {
                if (r != null) auths.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
            }
        }
        if (auths.isEmpty()) {
            auths = java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        UsernamePasswordAuthenticationToken userPrincipal = new UsernamePasswordAuthenticationToken(principalName, "n/a", auths);

        // Build authorization and generate access token
        Set<String> authorizedScopes = registeredClient.getScopes();
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userPrincipal)
                .authorizationServerContext(org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder.getContext())
                .tokenType(org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(GRANT_TYPE)
                .authorizationGrant(tokenRequest)
                .authorizedScopes(authorizedScopes)
                .build();
        OAuth2Token generatedToken = this.tokenGenerator.generate(tokenContext);
        if (generatedToken == null || !(generatedToken instanceof OAuth2AccessToken)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("server_error", "Token generation failed", null));
        }
        OAuth2AccessToken accessToken = (OAuth2AccessToken) generatedToken;

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(principalName)
                .authorizationGrantType(GRANT_TYPE)
                .attribute(OAuth2ParameterNames.USERNAME, principalName)
                .accessToken(accessToken)
                .build();
        authorizationService.save(authorization);

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("scope", String.join(" ", authorizedScopes));
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, null, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SkillratPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
