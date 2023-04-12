package com.crosschain.dispatch.transaction;

import com.crosschain.common.SystemInfo;
import com.crosschain.group.GroupManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class TransactionDispatcherConfig {

    @Resource
    private GroupManager groupManager;

    @Resource
    private SystemInfo systemInfo;

    @Bean
    public LockDispatcher lockDispatcher() {
        LockDispatcher lockDispatcher = new LockDispatcher();
        lockDispatcher.setGroupManager(groupManager);
        lockDispatcher.setSystemInfo(systemInfo);
        return lockDispatcher;
    }

    @Bean
    public UnlockDispatcher unlockDispatcher() {
        UnlockDispatcher lockDispatcher = new UnlockDispatcher();
        lockDispatcher.setGroupManager(groupManager);
        lockDispatcher.setSystemInfo(systemInfo);
        return lockDispatcher;
    }
}