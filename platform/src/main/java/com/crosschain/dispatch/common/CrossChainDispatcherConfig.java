package com.crosschain.dispatch.common;

import com.crosschain.group.GroupManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrossChainDispatcherConfig {

    @Resource
    private GroupManager groupManager;

    @Bean
    public CommonCrossChainDispatcherBase defaultDispatcher(){
        CommonCrossChainDispatcherBase dispatcher = new DefaultCommonCrossChainDispatcher();
        dispatcher.setChannelManager(groupManager);

        dispatcher.init();
        return dispatcher;
    }

    //todo 添加其他通信协议的dispatcher

}