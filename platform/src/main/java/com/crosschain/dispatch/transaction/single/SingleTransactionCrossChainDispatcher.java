package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.*;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
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

    /**
     * 源链锁定资产
     *
     * @param req
     * @param req_id
     * @return 锁定资产的结果
     * @throws Exception
     */
    CommonChainResponse processSrcLock(CommonChainRequest req, String req_id) throws Exception {

        log.info("[src chain do lock]:\n");
        String res = "";
        try {
            //通过BaseDispatcher 发起链合约调用
            res = sendTransaction(req);
            Chain chain = groupManager.getChain(req.getChainName());

            //处理跨链流程中的log或者过程信息
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "lock");
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
            auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));

            //处理哈希时间锁机制的过程信息
            Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
            //从请求参数中可以直接解析出来锁定资金和锁定时间的参数
            Matcher m = p.matcher(req.getArgs());
            String lock_amount = null;
            String lock_time = null;
            if (m.find()) {
                lock_amount = m.group(5);
                lock_time = m.group(6);
            }
            auditManager.addHTLCInfo(req_id, new HTLCMechanismInfo(lock_amount, lock_amount, lock_time));

            //根据锁定函数返回的结果判断锁定是否成功
            if (!CrossChainUtils.extractStatusField(res).equals("1")) {
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

        //判断源链解锁是否成功，若失败，则抛异常，后续回滚
        if (!CrossChainUtils.extractStatusField(res).equals("1")) {
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
        if (!CrossChainUtils.extractStatusField(res).equals("1")) {
            throw new CrossChainException(106, String.format("源链资产回滚失败,详情：%s", extractInfo("data", res)));
        }
        return new CommonChainResponse(res);
    }

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        String requestId = request.getRequestId();

        //设置上报数据的跨链机制
        auditManager.setMechanism(requestId, "1");

        CrossChainServiceResponse response = new CrossChainServiceResponse();

        //拆分源、目标链请求
        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        CommonChainRequest desChainRequest = request.getDesChainRequest();

        //上报的事务数据初始化
        TransactionAudit transAuditInfo = new TransactionAudit();
        String sender = null;
        String h = null;

        //add current timestamp，当前系统的时间是底层区块链合约需要的参数
        long current_time = System.currentTimeMillis();
        String ori = srcChainRequest.getArgs();
        ori = ori + "," + current_time;
        srcChainRequest.setArgs(ori);


        CommonChainResponse srcRes = null;
        try {
            //step1:源链锁资产
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
            //step2: 目标链执行合约
            desRes = processDes(desChainRequest, requestId);
            response.setDesResult(desRes.getResult());

            //判断目标链执行是否成功，若失败，则回滚
            if (CrossChainUtils.extractStatusField(desRes.getResult()).equals("1")) {
                //status==1 目标链合约执行结果表示成功，接下来执行unlock

                //要去源链锁定合约执行的结果中提取addr字段
                String lock_addr = extractInfo("addr", srcRes.getResult());
                //当前系统时间（区块链合约需要的）
                current_time = System.currentTimeMillis() / 1000;
                //组装解锁函数unlock的参数
                String unlock_args = sender + "," + h + "," + lock_addr + "," + current_time;
                srcChainRequest.setFunction("unlock");
                srcChainRequest.setArgs(unlock_args);

                //step3.1: actually do unlock,到此为止没有异常就流程结束了
                srcRes = processSrcUnlock(srcChainRequest, requestId);

                String final_src_resp = response.getSrcResult() + "[unlock]:\n" + srcRes.getResult();
                response.setSrcResult(final_src_resp);
            } else {
                //step3.2: status!=1 目标链合约执行结果表示失败，接下来执行rollback
                //rollback
                rollback(response, srcChainRequest, sender, h, requestId);
            }

            //流程结束后组装事务数据
            TransactionAudit.construct(transAuditInfo, groupManager.getGroup(request.getGroup()), auditManager, request, srcRes.getResult());
        } catch (LockException lockException) {
            //这里是锁定阶段出了错误，也就是说资产没有锁定，所以就不能回滚
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