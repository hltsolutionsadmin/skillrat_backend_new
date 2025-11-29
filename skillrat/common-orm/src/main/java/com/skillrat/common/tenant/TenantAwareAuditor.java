package com.skillrat.common.tenant;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class TenantAwareAuditor implements AuditorAware<String> {
    @SuppressWarnings("null")
	@Override
    public Optional<String> getCurrentAuditor() {
        String tenant = TenantContext.getTenantId();
        return Optional.ofNullable(tenant != null ? tenant + ":system" : "system");
    }
}
