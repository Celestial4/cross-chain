package com.crosschain.dispatch.transaction;

import com.crosschain.audit.AuditManager;
import com.crosschain.dispatch.transaction.duel.LockDispatcher;
import com.crosschain.dispatch.transaction.duel.UnlockDispatcher;
import com.crosschain.dispatch.transaction.duel.mode.EDispatcher;
import com.crosschain.dispatch.transaction.duel.mode.PDispatcher;
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
        lockDispatcher.setCrossChainMechanism(1);
        return lockDispatcher;
    }

    @Bean
    public UnlockDispatcher unlockDispatcher() {
        UnlockDispatcher unlockDispatcher = new UnlockDispatcher();
        unlockDispatcher.setGroupManager(groupManager);
        unlockDispatcher.setCrossChainMechanism(1);
        return unlockDispatcher;
    }

    //todo 添加其他通信协议的dispatcher
    @Bean
    public SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher() {
        SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher = new SingleTransactionCrossChainDispatcher();
        singleTransactionCrossChainDispatcher.setGroupManager(groupManager);
        singleTransactionCrossChainDispatcher.setAuditManager(auditManager);
        singleTransactionCrossChainDispatcher.setCrossChainMechanism(1);
        return singleTransactionCrossChainDispatcher;
    }

    @Bean
    public EDispatcher eDispatcher() {
        //NOTARY
        EDispatcher e = new EDispatcher("E");
        e.setGroupManager(groupManager);
        e.setAuditManager(auditManager);
        e.setCrossChainMechanism(2);
        return e;
    }

    @Bean
    PDispatcher pDispatcher() {
        //DPK
        PDispatcher p = new PDispatcher("P");
        p.setGroupManager(groupManager);
        p.setAuditManager(auditManager);
        p.setCrossChainMechanism(3);
        return p;
    }
}