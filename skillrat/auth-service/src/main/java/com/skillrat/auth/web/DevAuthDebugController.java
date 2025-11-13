package com.skillrat.auth.web;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Profile("dev")
@RestController
public class DevAuthDebugController {

    private final JdbcTemplate jdbcTemplate;

    public DevAuthDebugController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping(value = "/dev/auth/authorizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> listAuthorizations() {
        String sql = "select id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, length(access_token_value) as access_token_len, created_at from oauth2_authorization order by created_at desc";
        return jdbcTemplate.queryForList(sql);
    }
}
