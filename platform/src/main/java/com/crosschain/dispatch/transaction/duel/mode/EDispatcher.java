package com.crosschain.dispatch.transaction.duel.mode;

import com.crosschain.audit.entity.NotaryMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.common.SystemInfo;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EDispatcher extends OtherDispatcherBase {
    public EDispatcher(String m) {
        mode = m;
    }

    @Override
    protected String getMechanism() {
        return "2";
    }

    @Override
    protected ProcessAudit getProcessInfo(String result) {
        return new ProcessAudit("notary", result);
    }

    @Override
    protected void addMechanismInfo(String requestId, String result) throws UniException {
        try {
            String na_id = extractInfo("na_id", result);
            String na_choice = extractInfo("na_choice", result);
            String ns_ip = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName());
            NotaryMechanismInfo notaryMechanismInfo = new NotaryMechanismInfo(na_id, na_choice, ns_ip);
            auditManager.addNotaryInfo(requestId, notaryMechanismInfo);
        } catch (Exception e) {
            String msg = String.format("设置公证人组模式机制信息失败：%s", e.getMessage());
            log.error(msg);
            throw new CrossChainException(700,msg);
        }
    }
}