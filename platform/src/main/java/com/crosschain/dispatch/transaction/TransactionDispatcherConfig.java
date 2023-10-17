package com.crosschain.dispatch.transaction;

import com.crosschain.audit.AuditManager;
import com.crosschain.dispatch.transaction.dual.LockDispatcher;
import com.crosschain.dispatch.transaction.dual.UnlockDispatcher;
import com.crosschain.dispatch.transaction.dual.mode.EDispatcher;
import com.crosschain.dispatch.transaction.dual.mode.PDispatcher;
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

    @Bean("htlc")
    public SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher() {
        SingleTransactionCrossChainDispatcher singleTransactionCrossChainDispatcher = new SingleTransactionCrossChainDispatcher();
        singleTransactionCrossChainDispatcher.setGroupManager(groupManager);
        singleTransactionCrossChainDispatcher.setAuditManager(auditManager);
        singleTransactionCrossChainDispatcher.setCrossChainMechanism(1);
        return singleTransactionCrossChainDispatcher;
    }

    @Bean("notary")
    public EDispatcher eDispatcher() {
        //NOTARY
        EDispatcher e = new EDispatcher("E");
        e.setGroupManager(groupManager);
        e.setAuditManager(auditManager);
        e.setCrossChainMechanism(2);
        return e;
    }

    @Bean("dpky")
    PDispatcher pDispatcher() {
        //DPK
        PDispatcher p = new PDispatcher("P");
        p.setGroupManager(groupManager);
        p.setAuditManager(auditManager);
        p.setCrossChainMechanism(3);
        return p;
    }
}