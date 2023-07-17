package com.crosschain.dispatch.basic;

import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InfoSharingDispatcher extends BaseDispatcher {
    abstract protected CommonChainResponse processDes(CommonChainRequest req, String req_id) throws Exception;

    abstract protected CommonChainResponse processSrc(CommonChainRequest req, String req_id) throws Exception;

    abstract protected String processResult(CommonChainResponse rep);

    abstract protected void processAudit(TransactionAudit audit, CrossChainRequest req, String req_id, String status) throws Exception;

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        CrossChainServiceResponse response = new CrossChainServiceResponse();

        TransactionAudit auditInfo = new TransactionAudit();
        try {
        CommonChainRequest desChainRequest = request.getDesChainRequest();
        CommonChainResponse desRes = processDes(desChainRequest, request.getRequestId());
        response.setDesResult(desRes.getResult());

        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        if ("".equals(srcChainRequest.getArgs())) {
            //如果需要在源链上回写目标链合约结果，修改processResult（）函数
            srcChainRequest.setArgs(processResult(desRes));
        }
        CommonChainResponse srcRes = processSrc(srcChainRequest, request.getRequestId());
        response.setSrcResult(srcRes.getResult());

        //源链执行后上报数据
        processAudit(auditInfo, request, response.getDesResult()+"\n"+response.getSrcResult(), "1");
        }catch (Exception e) {
            processAudit(auditInfo, request, response.getDesResult()+"\n"+response.getSrcResult(),"2");
            throw e;
        }finally {
            auditManager.addTransactionInfo(request.getRequestId(), auditInfo);
            auditManager.uploadAuditInfo(request.getRequestId());
        }

        return response;
    }
}