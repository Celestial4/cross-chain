package com.crosschain.dispatch.transaction.dual.mode;

import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.UniException;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.entity.SelfServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class OtherDispatcherBase extends BaseDispatcher {

    protected String mode;

    protected abstract String getMechanism();

    protected abstract void setProcessInfo(String reqId, String result, CommonChainRequest req);

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
        //这个模式是区块链合约规定的  源链的参数，目标链名，目标链参数，模式
        String args = srcChainRequest.getArgs() + SPLITTER + desChainRequest.getChainName() + SPLITTER + desChainRequest.getArgs() + SPLITTER + mode;

        //复用srcRequest用于发起区块链合约调用
        srcChainRequest.setArgs(args);
        String result = "";
        TransactionAudit transAuditInfo = new TransactionAudit();
        try {
            //实际执行
            result = sendTransaction(srcChainRequest);

            CrossChainUtils.constructAuditInfo(transAuditInfo, groupManager, auditManager, req, result);

            finishCrosschain(result);

        } catch (UniException e) {
            log.warn(e.getErrorMsg());
            CrossChainUtils.constructErrorAuditInfo(transAuditInfo, req, groupManager, auditManager);
            throw e;
        } finally {
            //设置过程信息
            setProcessInfo(requestId, result, srcChainRequest);
            //设置机制信息
            addMechanismInfo(requestId, result);
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }
        return new SelfServiceResponse(result);

    }
}