package com.crosschain.auth;

import com.crosschain.datasource.UserAuthSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class AuthManagerConfig {

    @Resource
    private UserAuthSource db;

    @Bean
    public AuthManager authManager() {
        AuthManager manager = new AuthManager();
        manager.setDb(db);
        return manager;
    }
}