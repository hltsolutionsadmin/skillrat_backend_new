
package com.skillrat.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(
        name = "user-service",
        path = "/api/users",
        configuration = com.skillrat.project.config.FeignAuthConfig.class
)
public interface UserClient {

    @GetMapping("/me")
    Map<String, Object> me();
}