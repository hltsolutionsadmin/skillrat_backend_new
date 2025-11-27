package com.skillrat.agent.tools;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IncidentTools {

    private final WebClient incidentWebClient;

    public CreateIncidentResponse createIncident(CreateIncidentRequest req) {
        HttpHeaders headers = buildAuthHeaders();
        if (req.getIdempotencyKey() != null && !req.getIdempotencyKey().isBlank()) {
            headers.add("Idempotency-Key", req.getIdempotencyKey());
        }
        Map<String, Object> payload = Map.of(
                "category", req.getCategory(),
                "title", req.getTitle(),
                "description", req.getDescription(),
                "priority", req.getPriority(),
                "assignee", req.getAssignee(),
                "tags", req.getTags()
        );
        // POST /api/incidents expected from incident-service
        return incidentWebClient.post()
                .uri("/api/incidents")
                .headers(h -> h.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(CreateIncidentResponse.class)
                .onErrorResume(ex -> Mono.just(new CreateIncidentResponse(null, "FAILED", null, "Error: " + ex.getMessage())))
                .block();
    }

    public List<String> getIncidentCategories() {
        HttpHeaders headers = buildAuthHeaders();
        return incidentWebClient.get()
                .uri("/api/incidents/categories")
                .headers(h -> h.addAll(headers))
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .onErrorReturn(java.util.List.of())
                .block();
    }

    public WhoAmIResponse whoAmI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth != null ? auth.getName() : null;
        // In real setup, read tenant from header resolver or SecurityContext custom claims
        String tenantId = "default";
        return new WhoAmIResponse(name, tenantId, null);
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token && !token.isBlank()) {
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        // Optionally add tenant header if you use one globally
        // headers.add("X-Skillrat-Tenant", TenantContext.getTenantId());
        return headers;
    }

    @Data
    public static class CreateIncidentRequest {
        private String category;
        private String title;
        private String description;
        private String priority; // e.g. LOW, MEDIUM, HIGH
        private String assignee; // optional
        private List<String> tags; // optional
        private String idempotencyKey; // optional
    }

    @Data
    public static class CreateIncidentResponse {
        private String incidentId;
        private String status;
        private String url;
        private String message;

        public CreateIncidentResponse() {}
        public CreateIncidentResponse(String incidentId, String status, String url, String message) {
            this.incidentId = incidentId;
            this.status = status;
            this.url = url;
            this.message = message;
        }
    }

    @Data
    public static class WhoAmIResponse {
        private final String user;
        private final String tenantId;
        private final List<String> roles;
    }
}
