package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.HTLCMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.CrossChainException;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SingleTransactionCrossChainDispatcher extends BaseDispatcher {

    CommonChainResponse processDes(CommonChainRequest req, Group group) throws Exception {

        log.info("[des chain do]:\n");
        String res = sendTransaction(req);
        auditManager.addProcess(requestId, new ProcessAudit(new Date().toString(),"call contract of destination chain",res));
        Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
        Matcher m = p.matcher(req.getArgs());
        String lock_amount = null;
        String lock_time = null;
        if (m.find()) {
            lock_amount = m.group(5);
            lock_time = m.group(6);
        }
        auditManager.addHTLCInfo(requestId,new HTLCMechanismInfo(lock_amount,lock_amount,lock_time));
        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcLock(CommonChainRequest req, Group group) throws Exception {

        log.info("[src chain do]:\n");
        String res = sendTransaction(req);
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(104, String.format("源链资产锁定失败,请检查跨链参数或相应区块链，详情：%s", extractInfo("data",res)));
        }
        auditManager.addProcess(requestId,new ProcessAudit(new Date().toString(), "lock", res));

        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcUnlock(CommonChainRequest req, Group group) throws Exception {

        log.info("[src chain do]:\n");
        String res = sendTransaction(req);
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(105, String.format("源链资产解锁失败,详情：%s", extractInfo("data",res)));
        }
        auditManager.addProcess(requestId,new ProcessAudit(new Date().toString(), "unlock", res));
        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcRollback(CommonChainRequest req, Group group) throws Exception {

        log.info("[src chain do]:\n");
        String res = sendTransaction(req);
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(106, String.format("源链资产回滚失败,详情：%s", extractInfo("data",res)));
        }
        auditManager.addProcess(requestId,new ProcessAudit(new Date().toString(),"rollback",res));
        auditManager.getHTLCInfo(requestId).setHtlc_status("状态回滚，不解锁");
        return new CommonChainResponse(res);
    }

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        setLocalChain(request);

        auditManager.setMechanism(requestId,"1");

        Group group = groupManager.getGroup(request.getGroup());

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
        srcRes = processSrcLock(srcChainRequest, group);
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
            desRes = processDes(desChainRequest, group);
            response.setDesResult(desRes.getResult());

            //目标链执行成功
            if (extractInfo("status", desRes.getResult()).equals("1")) {
                String lock_addr = extractInfo("addr", srcRes.getResult());
                current_time = System.currentTimeMillis() / 1000;
                String unlock_args = sender + "," + h + "," + lock_addr + "," + current_time;
                srcChainRequest.setFunction("unlock");
                srcChainRequest.setArgs(unlock_args);

                //actually do unlock
                srcRes = processSrcUnlock(srcChainRequest, group);
                String final_src_resp = response.getSrcResult() + "[unlock]:\n" + srcRes.getResult();
                response.setSrcResult(final_src_resp);
            } else {
                //rollback
                rollback(group, response, srcChainRequest, sender, h);
            }
            //流程结束后上报事务数据
            transAuditInfo = TransactionAudit.construct(groupManager, auditManager, request, srcRes.getResult(), requestId);
        } catch (Exception e) {
            rollback(group, response, srcChainRequest, sender, h);
            transAuditInfo = TransactionAudit.construct(groupManager, auditManager, request, srcRes.getResult(), requestId);
            throw e;
        }finally {
            auditManager.addTransactionInfo(requestId,transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
            auditManager.completeRequest(requestId);
        }

        return response;
    }

    private void rollback(Group group, CrossChainServiceResponse response, CommonChainRequest
            srcChainRequest, String sender, String h) throws Exception {
        log.info("----rollback-----");
        String rollback_args = sender + "," + h;
        srcChainRequest.setFunction("rollback");
        srcChainRequest.setArgs(rollback_args);
        CommonChainResponse srcRes1 = processSrcRollback(srcChainRequest, group);
        String final_src_resp = response.getSrcResult() + "[rollbacked]:\n"+srcRes1.getResult();
        response.setSrcResult(final_src_resp);
    }
}