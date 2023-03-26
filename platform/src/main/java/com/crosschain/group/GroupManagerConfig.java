package com.crosschain.group;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupManagerConfig {

    @Resource
    private GroupSource db;

    @Bean
    public GroupManager channelManager() {
        GroupManager groupManager = new GroupManager();
        groupManager.setDs(db);
        groupManager.init();
        return groupManager;
    }
}