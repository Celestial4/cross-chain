package com.crosschain.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

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