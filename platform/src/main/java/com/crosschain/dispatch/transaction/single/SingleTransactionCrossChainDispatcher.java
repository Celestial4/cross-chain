package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.HTLCMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.CrossChainException;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SingleTransactionCrossChainDispatcher extends BaseDispatcher {

    CommonChainResponse processDes(CommonChainRequest req, String id) throws Exception {

        log.info("[des chain do]:\n");
        String res = sendTransaction(req);
        auditManager.addProcess(id, new ProcessAudit("call contract of destination chain", res));
        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcLock(CommonChainRequest req, String id) throws Exception {

        log.info("[src chain do lock]:\n");
        String res = sendTransaction(req);
        auditManager.addProcess(id, new ProcessAudit("lock", res));
        Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
        Matcher m = p.matcher(req.getArgs());
        String lock_amount = null;
        String lock_time = null;
        if (m.find()) {
            lock_amount = m.group(5);
            lock_time = m.group(6);
        }
        auditManager.addHTLCInfo(id, new HTLCMechanismInfo(lock_amount, lock_amount, lock_time));
        if (!extractInfo("status", res).equals("1")) {
            auditManager.addHTLCInfo(id, new HTLCMechanismInfo(lock_amount, lock_amount, "lock failed."));
            TransactionAudit au = new TransactionAudit();
            au.setStatus(2);
            auditManager.addTransactionInfo(id, au);
            auditManager.uploadAuditInfo(id);
            throw new CrossChainException(104, String.format("源链资产锁定失败,请检查跨链参数或相应区块链，详情：%s", extractInfo("data", res)));
        }

        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcUnlock(CommonChainRequest req, String id) throws Exception {

        log.info("[src chain do unlock]:\n");
        String res = sendTransaction(req);
        auditManager.addProcess(id, new ProcessAudit("unlock", res));
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(105, String.format("源链资产解锁失败,详情：%s", extractInfo("data", res)));
        }
        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcRollback(CommonChainRequest req, String id) throws Exception {

        log.info("[src chain do rollback]:\n");
        String res = sendTransaction(req);
        auditManager.addProcess(id, new ProcessAudit("rollback", res));
        auditManager.getHTLCInfo(id).setHtlc_status("状态回滚，不解锁");
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(106, String.format("源链资产回滚失败,详情：%s", extractInfo("data", res)));
        }
        return new CommonChainResponse(res);
    }

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        String requestId = request.getRequestId();

        auditManager.setMechanism(requestId, "1");

        CrossChainServiceResponse response = new CrossChainServiceResponse();

        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        CommonChainRequest desChainRequest = request.getDesChainRequest();

        //add current timestamp
        long current_time = System.currentTimeMillis() / 1000;
        String ori = srcChainRequest.getArgs();
        ori = ori + "," + current_time;
        srcChainRequest.setArgs(ori);

        //源链锁资产
        CommonChainResponse srcRes = null;

        srcRes = processSrcLock(srcChainRequest, requestId);
        response.setSrcResult("[lock]:\n" + srcRes.getResult() + "\n");

        //通过正则读取sender和原像
        Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
        Matcher m = p.matcher(srcChainRequest.getArgs());
        String sender = null;
        String h = null;
        if (m.find()) {
            sender = m.group(1);
            h = m.group(4);
        }

        //事务上报data
        TransactionAudit transAuditInfo = null;

        //do deschain or rollback
        CommonChainResponse desRes = null;
        try {
            desRes = processDes(desChainRequest, requestId);
            response.setDesResult(desRes.getResult());

            //目标链执行成功
            if (extractInfo("status", desRes.getResult()).equals("1")) {
                String lock_addr = extractInfo("addr", srcRes.getResult());
                current_time = System.currentTimeMillis() / 1000;
                String unlock_args = sender + "," + h + "," + lock_addr + "," + current_time;
                srcChainRequest.setFunction("unlock");
                srcChainRequest.setArgs(unlock_args);

                //actually do unlock
                srcRes = processSrcUnlock(srcChainRequest, requestId);
                String final_src_resp = response.getSrcResult() + "[unlock]:\n" + srcRes.getResult();
                response.setSrcResult(final_src_resp);
            } else {
                //rollback
                rollback(response, srcChainRequest, sender, h, requestId);
            }
            //流程结束后上报事务数据
            transAuditInfo = TransactionAudit.construct(groupManager, auditManager, request, srcRes.getResult(), requestId);
        } catch (Exception e) {
            rollback(response, srcChainRequest, sender, h, requestId);
            transAuditInfo = TransactionAudit.construct(groupManager, auditManager, request, srcRes.getResult(), requestId);
            throw e;
        } finally {
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }

        return response;
    }

    private void rollback(CrossChainServiceResponse response, CommonChainRequest
            srcChainRequest, String sender, String h, String id) throws Exception {
        log.info("----rollback-----");
        String rollback_args = sender + "," + h;
        srcChainRequest.setFunction("rollback");
        srcChainRequest.setArgs(rollback_args);
        CommonChainResponse srcRes1 = processSrcRollback(srcChainRequest, id);
        String final_src_resp = response.getSrcResult() + "[rollbacked]:\n" + srcRes1.getResult();
        response.setSrcResult(final_src_resp);
    }
}