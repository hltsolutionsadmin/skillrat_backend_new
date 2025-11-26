package com.skillrat.organisation.config;

import com.skillrat.common.tenant.TenantAwareAuditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public TenantAwareAuditor auditorAware() {
        return new TenantAwareAuditor();
    }
}
