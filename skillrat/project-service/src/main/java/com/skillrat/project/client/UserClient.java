
package com.skillrat.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "user-service",
        path = "/api/users",
        configuration = com.skillrat.project.config.FeignAuthConfig.class
)
public interface UserClient {

    @GetMapping("/me")
    Map<String, Object> me();

    @GetMapping("/internal/byEmail/{email}")
    ResponseEntity<Map<String, Object>> getByEmail(@PathVariable("email") String email);
}