package com.skillrat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillrat.auth")
public class AuthProperties {
    /** redis | jdbc */
    private String tokenStore = "jdbc";

    public String getTokenStore() { return tokenStore; }
    public void setTokenStore(String tokenStore) { this.tokenStore = tokenStore; }
}
