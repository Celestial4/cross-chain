package com.crosschain.filter;

import com.crosschain.auth.AuthManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RequestFilterConfig {

    @Resource
    private AuthManager authManager;

    @Bean
    public RequestFilter filter() {
        RequestFilter filter = new RequestFilter();
        filter.setAuthManager(authManager);
        return filter;
    }
}