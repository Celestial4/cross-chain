package com.crosschain.dispatch.transaction.dual.mode;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.HTLCMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DualDispatcher extends OtherDispatcherBase {


    public DualDispatcher(String m) {
        mode = m;
    }

    @Override
    protected String getMechanism() {
        return "1";
    }

    @Override
    protected void setProcessInfo(String reqId, String result, CommonChainRequest req) {
        Chain chain = null;
        String errorInfo = "";
        ProcessAudit processAudit = new ProcessAudit();
        try {
            try {
                errorInfo = extractInfo("data", result);
            } catch (Exception e) {
                //donothing
            }
            chain = groupManager.getChain(req.getChainName());
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, result, "htlc");
            processAudit.setProcess_log(processLog);
            processAudit.setProcess_result(result);
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(result);
            processAudit.setExtensionInfo(extensionInfo);
        } catch (Exception e) {
            //do nothing
            log.debug("获取哈希时间锁机制信息处理过程异常：" + e.getMessage());
            ProcessLog processLog = AuditUtils.buildErrorProcessLog(chain, result, "htlc occurs exception", errorInfo);
            processAudit.setProcess_log(processLog);
        } finally {
            auditManager.addProcess(reqId, processAudit);
        }
    }

    @Override
    protected void addMechanismInfo(String requestId, String result) throws UniException {
        HTLCMechanismInfo htlcMechanismInfo = new HTLCMechanismInfo();
        auditManager.addHTLCInfo(requestId, htlcMechanismInfo);

    }

    @Override
    protected void finishCrosschain(String result) throws UniException {
        if (!CrossChainUtils.extractStatusField(result).equals("1")) {
            String errorInfo = extractInfo("data", result);
            throw new CrossChainException(700, "哈希时间锁定跨链失败：" + errorInfo);
        }
    }
}
