package com.skillrat.auth.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckTokenController {

    private final OAuth2AuthorizationService authorizationService;
    private final JdbcTemplate jdbcTemplate;

    public CheckTokenController(OAuth2AuthorizationService authorizationService, JdbcTemplate jdbcTemplate) {
        this.authorizationService = authorizationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping(value = "/oauth/check_token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkTokenGet(@RequestParam("token") String token) {
        return doIntrospection(token);
    }

    @PostMapping(value = "/oauth/check_token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkTokenPost(@RequestParam("token") String token) {
        return doIntrospection(token);
    }

    private ResponseEntity<Map<String, Object>> doIntrospection(String tokenValue) {
        OAuth2Authorization auth = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        Map<String, Object> body = new HashMap<>();
        if (auth == null || auth.getAccessToken() == null || auth.getAccessToken().isInvalidated()) {
            // Fallback check: see if row exists in oauth2_authorization for this token value
            try {
                Integer cnt = jdbcTemplate.queryForObject(
                        "select count(*) from oauth2_authorization where access_token_value = ?",
                        Integer.class, tokenValue);
                if (cnt != null && cnt > 0) {
                    body.put("active", true);
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
                }
            } catch (Exception ignored) {
            }
            body.put("active", false);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        }
        body.put("active", true);

        OAuth2Authorization.Token<OAuth2AccessToken> at = auth.getAccessToken();
        OAuth2AccessToken token = at.getToken();
        if (token.getIssuedAt() != null) {
            body.put("iat", token.getIssuedAt().getEpochSecond());
            body.put("nbf", token.getIssuedAt().getEpochSecond());
        }
        if (token.getExpiresAt() != null) {
            body.put("exp", token.getExpiresAt().getEpochSecond());
        }
        if (token.getScopes() != null && !token.getScopes().isEmpty()) {
            body.put("scope", String.join(" ", token.getScopes()));
        }

        Map<String, Object> claims = at.getClaims();
        if (claims != null) {
            // include standard ones if present
            if (claims.containsKey("sub")) body.put("sub", claims.get("sub"));
            if (claims.containsKey("aud")) body.put("aud", claims.get("aud"));
            if (claims.containsKey("iss")) body.put("iss", claims.get("iss"));
            if (claims.containsKey("username")) body.put("username", claims.get("username"));
            if (claims.containsKey("roles")) body.put("roles", claims.get("roles"));
            if (claims.containsKey("tenant_id")) body.put("tenant_id", claims.get("tenant_id"));
            if (claims.containsKey("userId")) body.put("userId", claims.get("userId"));
        }

        body.put("client_id", auth.getRegisteredClientId());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
