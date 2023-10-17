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

        //现在的 异步处理跨链请求的情况下 该response没有意义了
        CrossChainServiceResponse response = new CrossChainServiceResponse();

        //初始化数据上报里面的事务数据
        TransactionAudit auditInfo = new TransactionAudit();
        try {
            //拆解目标链的请求（链具体要做的事情的封装）
            CommonChainRequest desChainRequest = request.getDesChainRequest();
            CommonChainResponse desRes = processDes(desChainRequest, request.getRequestId());
            response.setDesResult(desRes.getResult());

            //拆解源链的请求（链具体要做的事情的封装）
            CommonChainRequest srcChainRequest = request.getSrcChainRequest();
            //在信息共享跨链场景下，源链的请求参数按理说是应该由目标链的结果来确定，但是也提供了自定义的方法，也就是可以自己设置源链的请求参数
            if ("".equals(srcChainRequest.getArgs())) {
                //如果需要在源链上回写目标链合约结果，修改processResult（）函数
                srcChainRequest.setArgs(processResult(desRes));
            }
            CommonChainResponse srcRes = processSrc(srcChainRequest, request.getRequestId());
            response.setSrcResult(srcRes.getResult());

            //双方链执行完成后上报数据
            processAudit(auditInfo, request, response.getDesResult() + "\n" + response.getSrcResult(), "1");
        } catch (Exception e) {
            processAudit(auditInfo, request, response.getDesResult() + "\n" + response.getSrcResult(), "2");
            throw e;
        } finally {
            auditManager.addTransactionInfo(request.getRequestId(), auditInfo);
            auditManager.uploadAuditInfo(request.getRequestId());
        }

        return response;
    }
}