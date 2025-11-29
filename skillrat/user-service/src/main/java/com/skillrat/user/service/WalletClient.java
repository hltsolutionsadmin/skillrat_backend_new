package com.skillrat.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WalletClient {
    private final RestTemplate restTemplate;

    @Value("${skillrat.wallet.base-url:http://localhost:8084}")
    private String walletBaseUrl;

    public WalletClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings({ "null", "rawtypes" })
	public int award(UUID userId, String tenantId, String category, String reason, UUID relatedId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Skillrat-Tenant", tenantId);

            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("category", category);
            body.put("reason", reason);
            body.put("relatedId", relatedId);

            String url = walletBaseUrl + "/api/wallet/internal/award";
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null && resp.getBody().get("credited") != null) {
                return Integer.parseInt(resp.getBody().get("credited").toString());
            }
        } catch (Exception e) {
            // Log the error but don't fail the main operation
            e.printStackTrace();
        }
        return 0;
    }
}
