package com.crosschain.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemInfoConfig {

    @Bean
    public SystemInfo systemInfo() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.init();
        return systemInfo;
    }
}