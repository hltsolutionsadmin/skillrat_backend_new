package com.skillrat.auth.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class RefreshFromAccessController {

    private final OAuth2AuthorizationService authorizationService;

    public RefreshFromAccessController(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping(value = "/oauth/refresh-from-access", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refreshFromAccess(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        String tokenValue = extractBearer(authHeader).orElse(tokenParam);
        if (tokenValue == null || tokenValue.isBlank()) {
            return ResponseEntity.badRequest().body(error("invalid_request", "Missing Bearer access token"));
        }

        OAuth2Authorization authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null || authorization.getAccessToken() == null) {
            return ResponseEntity.status(401).body(error("invalid_token", "Access token not recognized"));
        }

        OAuth2Authorization.Token<OAuth2AccessToken> at = authorization.getAccessToken();
        if (at.getToken().getExpiresAt() != null && Instant.now().isAfter(at.getToken().getExpiresAt())) {
            return ResponseEntity.status(401).body(error("invalid_token", "Access token expired"));
        }

        Instant now = Instant.now();
        OAuth2RefreshToken newRefresh = new OAuth2RefreshToken(java.util.UUID.randomUUID().toString(), now, now.plus(Duration.ofDays(30)));

        OAuth2Authorization updated = OAuth2Authorization.from(authorization)
                .token(newRefresh)
                .build();
        authorizationService.save(updated);

        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", newRefresh.getTokenValue());
        body.put("expires_in", Duration.between(now, newRefresh.getExpiresAt()).getSeconds());
        if (at.getToken().getScopes() != null && !at.getToken().getScopes().isEmpty()) {
            body.put("scope", String.join(" ", at.getToken().getScopes()));
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private Optional<String> extractBearer(String header) {
        if (header == null) return Optional.empty();
        String prefix = "Bearer ";
        if (header.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return Optional.of(header.substring(prefix.length()).trim());
        }
        return Optional.empty();
    }

    private Map<String, Object> error(String code, String description) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", code);
        m.put("error_description", description);
        return m;
    }
}
