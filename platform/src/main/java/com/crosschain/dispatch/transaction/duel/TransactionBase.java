package com.crosschain.dispatch.transaction.duel;

import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TransactionBase extends BaseDispatcher {
    abstract protected String doDes(CommonChainRequest req, Group grp) throws Exception;

    abstract protected String doSrc(CommonChainRequest req, Group grp) throws Exception;

    abstract protected String getRollbackArgs(CommonChainRequest req) throws Exception;

    abstract protected void processLast(CrossChainRequest req, String unlockResult) throws Exception;

    @Override
    public CrossChainServiceResponse process(CrossChainRequest req) throws Exception {
        String requestId = req.getRequestId();
        Group group = groupManager.getGroup(req.getGroup());

        CrossChainServiceResponse response = new CrossChainServiceResponse();
        //上报的事务数据初始化
        TransactionAudit transAuditInfo = new TransactionAudit();
        try {
            String src_unlock_args = doSrc(req.getSrcChainRequest(), group);
            String des_unlock_args = doDes(req.getDesChainRequest(), group);

            //if unlock successfully, do upload audition info，这里是调用unlockDispatcher才会走到
            processLast(req, src_unlock_args);

            response.setDesResult(des_unlock_args);
            response.setSrcResult(src_unlock_args);

            //流程结束后组装事务数据
            //TransactionAudit.construct(transAuditInfo, groupManager.getGroup(req.getGroup()), auditManager, req, );
        } catch (Exception e) {
            rollBack(req, response);
            throw e;
        } finally {
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }
        return response;

    }

    private void rollBack(CrossChainRequest reqs, CrossChainServiceResponse response) throws Exception {
        CommonChainRequest srcChainRequest = reqs.getSrcChainRequest();
        CommonChainRequest desChainRequest = reqs.getDesChainRequest();

        log.info("rollback src chain");
        srcChainRequest.setFunction("rollback");
        String src_rollbackArgs = getRollbackArgs(srcChainRequest);
        srcChainRequest.setArgs(src_rollbackArgs);
        String src_rollback_res = sendTransaction(srcChainRequest);

        log.info("rollback des chain");
        desChainRequest.setFunction("rollback");
        String des_rollbackArgs = getRollbackArgs(desChainRequest);
        desChainRequest.setArgs(des_rollbackArgs);
        String des_rollback_res = sendTransaction(desChainRequest);


        response.setSrcResult(src_rollback_res);
        response.setDesResult(des_rollback_res);
    }
}