package com.skillrat.auth.service;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
public class TokenRevocationService {

    private final OAuth2AuthorizationService authorizationService;

    public TokenRevocationService(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public boolean revokeAccessToken(String tokenValue) {
        OAuth2Authorization auth = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (auth != null) {
            authorizationService.remove(auth);
            return true;
        }
        return false;
    }
}
