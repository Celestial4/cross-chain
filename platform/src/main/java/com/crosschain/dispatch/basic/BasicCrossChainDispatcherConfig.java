package com.crosschain.dispatch.basic;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.basic.mode.DefaultInfoSharingDispatcher;
import com.crosschain.group.GroupManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class BasicCrossChainDispatcherConfig {

    @Resource
    private GroupManager groupManager;

    @Resource
    private SystemInfo systemInfo;

    @Resource
    private AuditManager auditManager;

    @Bean
    public InfoSharingDispatcher defaultDispatcher(){
        InfoSharingDispatcher dispatcher = new DefaultInfoSharingDispatcher();
        dispatcher.setGroupManager(groupManager);
        dispatcher.setSystemInfo(systemInfo);
        dispatcher.setAuditManager(auditManager);
        return dispatcher;
    }

    //todo 添加其他通信协议的dispatcher

}