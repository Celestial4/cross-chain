package com.crosschain.dispatch.self;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SelfDispatcherConfig {

    @Bean("self")
    public SelfDispatcher selfDispatcher() {
        return new SelfDispatcher();
    }
}