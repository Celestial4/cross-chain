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
    protected void setProcessInfo(String req_id, String result) {

        auditManager.addProcess(req_id, new ProcessAudit("notary", result));
    }

    @Override
    protected void addMechanismInfo(String requestId, String result) throws UniException {
        NotaryMechanismInfo notaryMechanismInfo = new NotaryMechanismInfo();
        try {
            String ns_ip = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName());
            notaryMechanismInfo.setNs_ip(ns_ip);
            String na_id = extractInfo("na_id", result);
            String na_choice = extractInfo("na_choice", result);
            notaryMechanismInfo.setNa_choice(na_choice);
            notaryMechanismInfo.setNa_id(na_id);
            auditManager.addNotaryInfo(requestId, notaryMechanismInfo);
        } catch (Exception e) {
            String msg = String.format("设置公证人组模式机制信息失败：%s", e.getMessage());
            log.error(msg);
            notaryMechanismInfo.setNa_choice("error");
            notaryMechanismInfo.setNa_id("null");
            auditManager.addNotaryInfo(requestId, notaryMechanismInfo);
            throw new CrossChainException(700, msg);
        }
    }
}