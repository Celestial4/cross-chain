package com.crosschain.dispatch;

import com.crosschain.channel.ChannelManager;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrossChainDispatcherConfig {

    @Resource
    private ChannelManager channelManager;

    @Bean
    public DispatcherBase defaultDispatcher(){
        DispatcherBase dispatcher = new DefaultDispatcher();
        dispatcher.setChannelManager(channelManager);

        dispatcher.init();
        return dispatcher;
    }

    //todo 添加其他通信协议的dispatcher

}