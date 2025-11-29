package com.skillrat.auth.config;

import java.time.Duration;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class TokenStoreConfig {

    @Bean
    @ConditionalOnProperty(prefix = "skillrat.auth", name = "token-store", havingValue = "jdbc", matchIfMissing = true)
    public OAuth2AuthorizationService jdbcAuthorizationService(@NonNull DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    @ConditionalOnProperty(prefix = "skillrat.auth", name = "token-store", havingValue = "redis")
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @ConditionalOnProperty(prefix = "skillrat.auth", name = "token-store", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnProperty(prefix = "skillrat.auth", name = "token-store", havingValue = "redis")
    public OAuth2AuthorizationService redisAuthorizationService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisAuthorizationService(redisTemplate, Duration.ofHours(12));
        // TTL aligned with access token TTL; adjust per property later
    }
}
