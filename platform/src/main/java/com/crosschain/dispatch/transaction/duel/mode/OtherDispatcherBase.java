package com.crosschain.dispatch.transaction.duel.mode;

import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.entity.SelfServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class OtherDispatcherBase extends BaseDispatcher {

    protected String mode;

    protected abstract String getMechanism();

    protected abstract void setProcessInfo(String reqId, String result);

    protected abstract void addMechanismInfo(String requestId, String result) throws Exception;

    protected abstract void finishCrosschain(String result) throws Exception;

    @Override
    public Response process(CrossChainRequest req) throws Exception {
        String requestId = req.getRequestId();
        //设置跨链机制
        auditManager.setMechanism(requestId, getMechanism());

        CommonChainRequest srcChainRequest = req.getSrcChainRequest();
        CommonChainRequest desChainRequest = req.getDesChainRequest();

        //do crosschain
        String SPLITTER = ",";
        String args = srcChainRequest.getArgs() + SPLITTER + desChainRequest.getChainName() + SPLITTER + desChainRequest.getArgs() + SPLITTER + mode;
        srcChainRequest.setArgs(args);
        String result = "";
        TransactionAudit transAuditInfo = new TransactionAudit();
        try {
            result = sendTransaction(srcChainRequest);

            TransactionAudit.construct(transAuditInfo, groupManager.getGroup(req.getGroup()), auditManager, req, result);

            finishCrosschain(result);

        } catch (Exception e) {
            log.error(e.getMessage());
            TransactionAudit.setErrorCallAuditInfo(transAuditInfo, req, groupManager.getGroup(req.getGroup()), auditManager);
            throw e;
        } finally {
            //设置过程信息
            setProcessInfo(requestId, result);
            //设置机制信息
            addMechanismInfo(requestId, result);
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }
        return new SelfServiceResponse(result);

    }
}