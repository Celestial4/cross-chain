package com.crosschain.dispatch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrossChainDispatcherConfig {

    @Bean
    public CrossChainDispatcher newCrossChainDispatcher(){
        CrossChainDispatcher dispatcher = new CrossChainDispatcher();
        dispatcher.init();
        return dispatcher;
    }
}