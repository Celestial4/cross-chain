package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.*;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.LockException;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SingleTransactionCrossChainDispatcher extends BaseDispatcher {

    CommonChainResponse processDes(CommonChainRequest req, String req_id) throws Exception {

        log.info("[des chain do]:\n");
        String res = sendTransaction(req);
        Chain chain = groupManager.getChain(req.getChainName());
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "call dest chain");
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));

        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcLock(CommonChainRequest req, String req_id) throws Exception {

        log.info("[src chain do lock]:\n");
        String res = "";
        try {
            res = sendTransaction(req);
            Chain chain = groupManager.getChain(req.getChainName());
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "lock");
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
            auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));
            Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
            Matcher m = p.matcher(req.getArgs());
            String lock_amount = null;
            String lock_time = null;
            if (m.find()) {
                lock_amount = m.group(5);
                lock_time = m.group(6);
            }
            auditManager.addHTLCInfo(req_id, new HTLCMechanismInfo(lock_amount, lock_amount, lock_time));
            if (!extractInfo("status", res).equals("1")) {
                auditManager.addHTLCInfo(req_id, new HTLCMechanismInfo(lock_amount, lock_amount, "lock failed."));
                throw new CrossChainException(104, String.format("源链资产锁定失败,请检查跨链参数或相应区块链，详情：%s", extractInfo("data", res)));
            }
        } catch (Exception e) {
            throw new LockException("lock failed");
        }

        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcUnlock(CommonChainRequest req, String req_id) throws Exception {

        log.info("[src chain do unlock]:\n");
        String res = sendTransaction(req);
        Chain chain = groupManager.getChain(req.getChainName());
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "unlock");
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));
        if (!extractInfo("status", res).equals("1")) {
            throw new CrossChainException(105, String.format("源链资产解锁失败,详情：%s", extractInfo("data", res)));
        }
        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrcRollback(CommonChainRequest req, String req_id) throws Exception {

        log.info("[src chain do rollback]:\n");
        String res = sendTransaction(req);
        Chain chain = groupManager.getChain(req.getChainName());
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "rollback");
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));
        auditManager.getHTLCInfo(req_id).setHtlc_status("状态回滚，不解锁");
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

        //事务上报data
        TransactionAudit transAuditInfo = new TransactionAudit();
        String sender = null;
        String h = null;

        //add current timestamp
        long current_time = System.currentTimeMillis();
        String ori = srcChainRequest.getArgs();
        ori = ori + "," + current_time;
        srcChainRequest.setArgs(ori);

        //源链锁资产
        CommonChainResponse srcRes = null;
        try {
            srcRes = processSrcLock(srcChainRequest, requestId);
            response.setSrcResult("[lock]:\n" + srcRes.getResult() + "\n");

            //通过正则读取sender和原像
            Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
            Matcher m = p.matcher(srcChainRequest.getArgs());

            if (m.find()) {
                sender = m.group(1);
                h = m.group(4);
            }

            //do deschain or rollback
            CommonChainResponse desRes = null;

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
            TransactionAudit.construct(transAuditInfo, groupManager.getGroup(request.getGroup()), auditManager, request, srcRes.getResult());
        } catch (LockException lockException) {
            TransactionAudit.setErrorCallAuditInfo(transAuditInfo, request, groupManager.getGroup(request.getGroup()), auditManager);
        } catch (Exception e) {
            //除了锁定错误外，其他出错都要回滚
            rollback(response, srcChainRequest, sender, h, requestId);
            TransactionAudit.construct(transAuditInfo, groupManager.getGroup(request.getGroup()), auditManager, request, srcRes.getResult());
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