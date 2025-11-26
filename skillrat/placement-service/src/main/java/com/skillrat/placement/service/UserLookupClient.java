package com.skillrat.placement.service;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserLookupClient {
    private final RestTemplate restTemplate;
    public UserLookupClient(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    public Optional<UUID> findB2bUnitIdByEmail(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Skillrat-Tenant", Optional.ofNullable(TenantContext.getTenantId()).orElse("default"));
        ResponseEntity<Map> resp = restTemplate.exchange("http://user-service:8080/api/users/internal/by-email?email=" + email,
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return Optional.empty();
        Object id = resp.getBody().get("b2bUnitId");
        return id == null ? Optional.empty() : Optional.of(UUID.fromString(id.toString()));
    }
}
