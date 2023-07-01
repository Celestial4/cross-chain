package com.crosschain.group;

import javax.annotation.Resource;

import com.crosschain.datasource.GroupAndChainSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupManagerConfig {

    @Resource
    private GroupAndChainSource db;

    @Bean
    public GroupManager channelManager() {
        GroupManager groupManager = new GroupManager();
        groupManager.setDs(db);
        groupManager.init();
        return groupManager;
    }
}