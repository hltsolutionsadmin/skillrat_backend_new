package com.skillrat.user.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public record UserInfo(String userId, String email, String username, Set<String> roles, Map<String, Object> claims) {}

    public UserInfo get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authentication present");
        }

        String userId = null;
        String email = null;
        String username = null;
        Map<String, Object> claims = Map.of();

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            claims = jwt.getClaims();
            userId = firstNonNull(
                (String) claims.get("userId"),
                (String) claims.get("user_id"),
                (String) claims.get("sub"),
                (String) claims.get("uid"),
                (String) claims.get("username"),
                (String) claims.get("id")
            );
            email = firstNonNull(
                (String) claims.get("email"),
                (String) claims.get("user_email"),
                (String) claims.get("mail"),
                (String) claims.get("preferred_username")
            );
            username = firstNonNull(
                (String) claims.get("username"),
                (String) claims.get("preferred_username")
            );
            if (email == null) {
                Object un = claims.get("username");
                if (un instanceof String s && s.contains("@")) {
                    email = s;
                }
            }
            if (email == null) {
                Object sub = claims.get("sub");
                if (sub instanceof String s && s.contains("@")) {
                    email = s;
                }
            }
        } else if (auth instanceof BearerTokenAuthentication bta && bta.getTokenAttributes() != null) {
            claims = bta.getTokenAttributes();
            userId = firstNonNull(
                (String) claims.get("userId"),
                (String) claims.get("user_id"),
                (String) claims.get("sub"),
                (String) claims.get("uid"),
                (String) claims.get("username"),
                (String) claims.get("id")
            );
            email = firstNonNull(
                (String) claims.get("email"),
                (String) claims.get("user_email"),
                (String) claims.get("mail"),
                (String) claims.get("preferred_username")
            );
            username = firstNonNull(
                (String) claims.get("username"),
                (String) claims.get("preferred_username")
            );
            if (email == null) {
                Object un = claims.get("username");
                if (un instanceof String s && s.contains("@")) {
                    email = s;
                }
            }
            if (email == null) {
                Object sub = claims.get("sub");
                if (sub instanceof String s && s.contains("@")) {
                    email = s;
                }
            }
        }

        if (userId == null && auth.getName() != null && !auth.getName().isBlank()) {
            userId = auth.getName();
        }
        if (email == null && auth.getName() != null && auth.getName().contains("@")) {
            email = auth.getName();
        }
        if (username == null && auth.getName() != null && !auth.getName().isBlank()) {
            username = auth.getName();
        }

        Set<String> roles = toSet(auth.getAuthorities());
        return new UserInfo(userId, email, username, roles, claims);
    }

    public String id() { return get().userId(); }
    public String email() { return get().email(); }
    public String username() { return get().username(); }
    public Set<String> roles() { return get().roles(); }
    public Map<String, Object> claims() { return get().claims(); }

    private static Set<String> toSet(Collection<? extends GrantedAuthority> authorities) {
        return authorities == null ? Set.of() : authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... vals) {
        for (T v : vals) if (v != null) return v;
        return null;
    }
}
