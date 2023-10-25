package com.crosschain.dispatch.transaction;

import com.crosschain.audit.AuditManager;
import com.crosschain.dispatch.transaction.dual.LockDispatcher;
import com.crosschain.dispatch.transaction.dual.UnlockDispatcher;
import com.crosschain.dispatch.transaction.dual.mode.EDispatcher;
import com.crosschain.dispatch.transaction.dual.mode.PDispatcher;
import com.crosschain.dispatch.transaction.single.DualTransactionDispatcher;
import com.crosschain.dispatch.transaction.single.SingleTransactionDispatcher;
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
    public SingleTransactionDispatcher singleTransactionCrossChainDispatcher() {
        SingleTransactionDispatcher singleTransactionDispatcher = new SingleTransactionDispatcher();
        singleTransactionDispatcher.setGroupManager(groupManager);
        singleTransactionDispatcher.setAuditManager(auditManager);
        singleTransactionDispatcher.setCrossChainMechanism(1);
        return singleTransactionDispatcher;
    }

    @Bean("htlc2")
    public DualTransactionDispatcher dualDispatcher() {
        DualTransactionDispatcher dualTransactionDispatcher = new DualTransactionDispatcher();
        dualTransactionDispatcher.setGroupManager(groupManager);
        dualTransactionDispatcher.setAuditManager(auditManager);
        dualTransactionDispatcher.setCrossChainMechanism(1);
        return dualTransactionDispatcher;
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