package com.crosschain.dispatch.transaction.duel.mode;

import com.crosschain.audit.entity.DPKMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.common.SystemInfo;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PDispatcher extends OtherDispatcherBase {
    public PDispatcher(String m) {
        mode = m;
    }

    @Override
    protected String getMechanism() {
        return "3";
    }

    @Override
    protected ProcessAudit getProcessInfo(String result) {
        return new ProcessAudit("dpky", result);
    }

    @Override
    protected void addMechanismInfo(String requestId, String result) throws UniException {
        try {
            String dpky_id = extractInfo("dpky_id", result);
            String dpky = extractInfo("dpky", result);
            String dpky_ip = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName());
            DPKMechanismInfo dpkMechanismInfo = new DPKMechanismInfo(dpky_id, dpky_ip, dpky);
            auditManager.addDpkInfo(requestId, dpkMechanismInfo);
        } catch (Exception e) {
            String msg = String.format("设置分布式密钥控制模式机制信息失败：%s", e.getMessage());
            log.error(msg);
            throw new CrossChainException(800,msg);
        }
    }
}