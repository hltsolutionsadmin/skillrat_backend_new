package com.skillrat.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillrat.gateway")
public class GatewayProperties {
    private int checkTokenCacheTtlSeconds = 30;
    private boolean strictRevocation = false;
    private String introspectionUrl = "http://auth-service:8080/oauth/check_token";
    private String clientId = "gateway";
    private String clientSecret = "gateway-secret";
    private String baseDomain = "skillrat.com";

    public int getCheckTokenCacheTtlSeconds() { return checkTokenCacheTtlSeconds; }
    public void setCheckTokenCacheTtlSeconds(int v) { this.checkTokenCacheTtlSeconds = v; }
    public boolean isStrictRevocation() { return strictRevocation; }
    public void setStrictRevocation(boolean strictRevocation) { this.strictRevocation = strictRevocation; }
    public String getIntrospectionUrl() { return introspectionUrl; }
    public void setIntrospectionUrl(String introspectionUrl) { this.introspectionUrl = introspectionUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getBaseDomain() { return baseDomain; }
    public void setBaseDomain(String baseDomain) { this.baseDomain = baseDomain; }
}
