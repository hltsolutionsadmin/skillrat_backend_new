package com.skillrat.auth.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.util.Assert;

import java.time.Duration;

public class RedisAuthorizationService implements OAuth2AuthorizationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration ttl;

    public RedisAuthorizationService(RedisTemplate<String, Object> redisTemplate, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    private String key(String token) {
        return "tenant:" + "global" + ":oauth2:auth:" + token; // tenant prefix adaptable later
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        if (authorization.getAccessToken() != null) {
            String token = authorization.getAccessToken().getToken().getTokenValue();
            redisTemplate.opsForValue().set(key(token), authorization, ttl);
        }
        if (authorization.getRefreshToken() != null) {
            String token = authorization.getRefreshToken().getToken().getTokenValue();
            redisTemplate.opsForValue().set(key(token), authorization, ttl);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        if (authorization == null) return;
        if (authorization.getAccessToken() != null) {
            String token = authorization.getAccessToken().getToken().getTokenValue();
            redisTemplate.delete(key(token));
        }
        if (authorization.getRefreshToken() != null) {
            String token = authorization.getRefreshToken().getToken().getTokenValue();
            redisTemplate.delete(key(token));
        }
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return null; // not used in this scaffold
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Object obj = redisTemplate.opsForValue().get(key(token));
        if (obj instanceof OAuth2Authorization auth) {
            return auth;
        }
        return null;
    }
}
