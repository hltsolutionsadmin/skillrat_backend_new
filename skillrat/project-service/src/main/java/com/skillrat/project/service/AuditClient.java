package com.skillrat.project.service;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditClient {

    private final RestTemplate restTemplate;

    @Value("${skillrat.audit.base-url:http://localhost:8086}")
    private String auditBaseUrl;

    public AuditClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void logChange(String entityType,
                          UUID entityId,
                          String action,
                          String fieldName,
                          String oldValue,
                          String newValue,
                          String changedBy) {
        String tenantId = TenantContext.getTenantId();

        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", "project-service");
        body.put("entityType", entityType);
        body.put("entityId", entityId != null ? entityId.toString() : null);
        body.put("action", action);
        body.put("fieldName", fieldName);
        body.put("oldValue", oldValue);
        body.put("newValue", newValue);
        body.put("changedBy", changedBy);
        body.put("changedAt", java.time.Instant.now());
        body.put("tenantId", tenantId);

        try {
            restTemplate.postForEntity(auditBaseUrl + "/api/audit/logs", body, Void.class);
        } catch (Exception ignored) {
            // do not break main flow if audit-service is unavailable
        }
    }
}
