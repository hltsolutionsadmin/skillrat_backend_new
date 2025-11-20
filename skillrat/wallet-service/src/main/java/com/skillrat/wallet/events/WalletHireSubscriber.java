package com.skillrat.wallet.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.listener.ChannelTopic;

import org.springframework.data.redis.listener.PatternTopic;

import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Configuration
class WalletRedisConfig {

    @Bean

    @ConditionalOnProperty(prefix = "wallet.events.redis", name = "enabled", havingValue = "true", matchIfMissing = false)
    RedisMessageListenerContainer listenerContainer(RedisConnectionFactory connectionFactory, MessageListener walletListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(walletListener, new PatternTopic("tenant:*:events:placement:hire"));

        return container;

    }

}

@Component
class WalletHireListener implements MessageListener {

    private final StringRedisTemplate redisTemplate;

    public WalletHireListener(StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;

    }

    @Override

    public void onMessage(Message message, byte[] pattern) {

        String payload = message.toString();

        String[] parts = payload.split(",");

        if (parts.length < 3) return;

        String placementId = parts[0];

        String userId = parts[1];

        String amount = parts[2];

        String lockKey = "lock:wallet:credit:" + userId + ":" + placementId;

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(30));

        if (Boolean.TRUE.equals(acquired)) {

            try {

                // credit wallet - scaffold placeholder

                redisTemplate.opsForValue().increment("wallet:balance:" + userId, Long.parseLong(amount));

            } finally {

                redisTemplate.delete(lockKey);

            }

        }

    }

}

