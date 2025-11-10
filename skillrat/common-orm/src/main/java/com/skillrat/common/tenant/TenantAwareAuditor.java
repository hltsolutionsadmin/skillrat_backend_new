package com.skillrat.common.tenant;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class TenantAwareAuditor implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String tenant = TenantContext.getTenantId();
        // In real impl, combine tenant and user principal. For scaffold, use tenant or "system".
        return Optional.ofNullable(tenant != null ? tenant + ":system" : "system");
    }
}
