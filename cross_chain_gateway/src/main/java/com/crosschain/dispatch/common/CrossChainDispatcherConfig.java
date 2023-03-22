package com.crosschain.dispatch.common;

import com.crosschain.channel.ChannelManager;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrossChainDispatcherConfig {

    @Resource
    private ChannelManager channelManager;

    @Bean
    public CommonCrossChainDispatcherBase defaultDispatcher(){
        CommonCrossChainDispatcherBase dispatcher = new DefaultCommonCrossChainDispatcher();
        dispatcher.setChannelManager(channelManager);

        dispatcher.init();
        return dispatcher;
    }

    //todo 添加其他通信协议的dispatcher

}