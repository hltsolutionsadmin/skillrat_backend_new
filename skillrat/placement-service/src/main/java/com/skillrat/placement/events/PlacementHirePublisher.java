package com.skillrat.placement.events;

import com.skillrat.common.tenant.TenantContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlacementHirePublisher {

    private final StringRedisTemplate redisTemplate;

    public PlacementHirePublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishHire(String placementId, String userId, String amount) {
        String tenant = TenantContext.getTenantId();
        if (tenant == null) tenant = "default";
        String channel = "tenant:" + tenant + ":events:placement:hire";
        String payload = placementId + "," + userId + "," + amount;
        redisTemplate.convertAndSend(channel, payload);
    }
}
