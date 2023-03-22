package com.crosschain.auth;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthManagerConfig {

    @Resource
    private AuthSource db;

    @Bean
    public AuthManager authManager() {
        AuthManager manager = new AuthManager();
        manager.setDb(db);
        return manager;
    }
}