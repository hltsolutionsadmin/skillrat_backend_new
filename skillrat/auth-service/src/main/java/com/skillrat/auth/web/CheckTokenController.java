package com.skillrat.auth.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckTokenController {

    private final OAuth2AuthorizationService authorizationService;

    public CheckTokenController(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/oauth/check_token")
    public ResponseEntity<Map<String, Object>> checkToken(@RequestParam("token") String token) {
        OAuth2Authorization auth = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        Map<String, Object> body = new HashMap<>();
        if (auth == null || auth.getAccessToken() == null || auth.getAccessToken().isInvalidated()) {
            body.put("active", false);
            return ResponseEntity.ok(body);
        }
        body.put("active", true);
        Map<String, Object> claims = auth.getAccessToken().getClaims();
        if (claims != null) {
            body.putAll(claims);
        }
        body.put("client_id", auth.getRegisteredClientId());
        return ResponseEntity.ok(body);
    }
}
