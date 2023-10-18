package com.crosschain.dispatch.transaction.dual.mode;

import com.crosschain.audit.entity.NotaryMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
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
    protected void setProcessInfo(String req_id, String result, CommonChainRequest req) {
        Chain chain = null;
        ProcessAudit processAudit = new ProcessAudit();
        try {
            chain = groupManager.getChain(req.getChainName());
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, result, "notory");
            processAudit.setProcess_log(processLog);
            processAudit.setProcess_result(result);
            String na_time = extractInfo("na_time", result);
            processAudit.setProcess_time(na_time);
        } catch (Exception e) {
            //do nothing
            log.debug("获取公证人处理过程异常：" + e.getMessage());
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, result, "notory occurs exception");
            processAudit.setProcess_log(processLog);
        } finally {
            auditManager.addProcess(req_id, processAudit);
        }
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
        } catch (Exception e) {
            //do nothing
            log.debug("获取公证人机制信息异常：" + e.getMessage());
        } finally {
            auditManager.addNotaryInfo(requestId, notaryMechanismInfo);
        }
    }

    @Override
    protected void finishCrosschain(String result) throws Exception {
        if (!extractInfo("status", result).equals("1")) {
            String errorInfo = extractInfo("data", result);
            throw new CrossChainException(700, "公证人跨链失败：" + errorInfo);
        }
    }
}