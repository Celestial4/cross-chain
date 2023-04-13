package com.crosschain.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditManagerConfig {

    @Bean
    public AuditManager auditManager() {
        return new AuditManager();
    }
}