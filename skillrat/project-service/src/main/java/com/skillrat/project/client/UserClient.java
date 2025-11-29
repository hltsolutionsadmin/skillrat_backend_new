
package com.skillrat.project.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.skillrat.common.dto.UserDTO;

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

    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId) throws Exception;

    @PostMapping("/internal/byIds")
    List<UserDTO> getUsersByIds(@RequestBody Map<String, List<UUID>> body);
}