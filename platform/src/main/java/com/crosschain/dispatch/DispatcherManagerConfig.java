package com.crosschain.dispatch;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatcherManagerConfig {

    @Resource
    ApplicationContext app;

    @Bean
    public DispatcherManager dispatcherManager() {
        DispatcherManager manager = new DispatcherManager();
        manager.init(app);
        return manager;
    }
}