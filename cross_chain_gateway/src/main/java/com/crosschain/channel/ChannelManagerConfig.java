package com.crosschain.channel;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChannelManagerConfig {

    @Resource
    private ChannelSource db;

    @Bean
    public ChannelManager channelManager() {
        ChannelManager channelManager = new ChannelManager();
        channelManager.setDs(db);
        channelManager.init();
        return channelManager;
    }
}