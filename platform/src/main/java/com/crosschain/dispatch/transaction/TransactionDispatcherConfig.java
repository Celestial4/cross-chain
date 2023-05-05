package com.crosschain.dispatch.transaction;

import com.crosschain.audit.AuditManager;
import com.crosschain.dispatch.transaction.duel.LockDispatcher;
import com.crosschain.dispatch.transaction.duel.UnlockDispatcher;
import com.crosschain.dispatch.transaction.single.SingleTransactionCrossChainDispatcher;
import com.crosschain.group.GroupManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class TransactionDispatcherConfig {

    @Resource
    private GroupManager groupManager;

    @Resource
    private AuditManager auditManager;

    @Bean
    public LockDispatcher lockDispatcher() {
        LockDispatcher lockDispatcher = new LockDispatcher();
        lockDispatcher.setGroupManager(groupManager);
        return lockDispatcher;
    }

    @Bean
    public UnlockDispatcher unlockDispatcher() {
        UnlockDispatcher lockDispatcher = new UnlockDispatcher();
        lockDispatcher.setGroupManager(groupManager);
        return lockDispatcher;
    }

    //todo 添加其他通信协议的dispatcher
    @Bean
    public SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher(){
        SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher = new SingleTransactionCrossChainDispatcher();
        singleTransactionCrossChainDispatcher.setGroupManager(groupManager);
        singleTransactionCrossChainDispatcher.setAuditManager(auditManager);
        return singleTransactionCrossChainDispatcher;
    }
}