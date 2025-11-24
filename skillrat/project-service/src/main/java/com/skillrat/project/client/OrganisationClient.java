package com.skillrat.project.client;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrganisationClient {

    private final RestTemplate restTemplate;

    @Value("${skillrat.organisation.base-url:http://localhost:8082}")
    private String organisationBaseUrl;

    public OrganisationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean existsOrganisation(UUID organisationId) {
        if (organisationId == null) return false;
        String tenant = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Skillrat-Tenant", tenant);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String tokenValue = jwtAuth.getToken().getTokenValue();
            if (tokenValue != null && !tokenValue.isBlank()) {
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
            }
        }

        String url = organisationBaseUrl + "/api/b2b/" + organisationId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> resp = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Object.class);
            return resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
