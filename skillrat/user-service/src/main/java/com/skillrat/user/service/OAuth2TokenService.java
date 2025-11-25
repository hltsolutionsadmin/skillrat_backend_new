package com.skillrat.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;

@Service
public class OAuth2TokenService {

    private final RestTemplate restTemplate;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    public OAuth2TokenService(
            RestTemplate restTemplate,
            @Value("${spring.security.oauth2.client.provider.skillrat.token-uri}") String tokenUrl,
            @Value("${spring.security.oauth2.client.registration.skillrat.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.skillrat.client-secret}") String clientSecret) {
        this.restTemplate = restTemplate;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getTokenForUser(String username, String password) {
        try {
            log.info("Requesting OAuth2 token for user: {}", username);
            log.debug("Token URL: {}", tokenUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            // Create Basic Auth header
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            log.debug("Using client ID: {}", clientId);

            // Prepare form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);
            formData.add("scope", "read write");

            // Create request entity
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            // Log the request details (without sensitive data)
            log.debug("Sending OAuth2 token request to: {}", tokenUrl);
            log.debug("Request headers: {}", headers);
            log.debug("Form data: grant_type=password, username={}, scope=read write", username);

            // Make the request
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    OAuth2TokenResponse.class
            );

            log.debug("OAuth2 token response status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully obtained OAuth2 token for user: {}", username);
                return response.getBody().getAccessToken();
            } else {
                log.error("Failed to obtain OAuth2 token. Status: {}, Response: {}", 
                         response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error obtaining OAuth2 token for user: " + username, e);
        }
        
        return null;
    }

    // Simple POJO to map the token response
    public static class OAuth2TokenResponse {
        private String access_token;
        private String token_type;
        private long expires_in;
        private String scope;
        private String jti;

        // Getters and setters
        public String getAccessToken() { return access_token; }
        public void setAccessToken(String access_token) { this.access_token = access_token; }
        public String getTokenType() { return token_type; }
        public void setTokenType(String token_type) { this.token_type = token_type; }
        public long getExpiresIn() { return expires_in; }
        public void setExpiresIn(long expires_in) { this.expires_in = expires_in; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getJti() { return jti; }
        public void setJti(String jti) { this.jti = jti; }
    }
}
