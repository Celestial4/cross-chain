package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.LockException;
import com.crosschain.service.response.UniResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 单边哈希时间锁
 */
@Slf4j
public class SingleTransactionDispatcher extends HTLCBaseDispatcher {

    /**
     * 目标链执行合约
     *
     * @param req
     * @param req_id
     * @return 合约的执行结果，结果中含有status字段表示同意与否
     * @throws Exception
     */
    String processDes(CommonChainRequest req, String req_id) throws Exception {

        log.info("[des chain:{} do]:\n", req.getChainName());
        String res = sendTransaction(req);

        Chain chain = groupManager.getChain(req.getChainName());
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "call dest chain");
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));

        return res;
    }

    @Override
    public UniResponse process(CrossChainRequest request) throws Exception {
        String requestId = request.getRequestId();

        //设置上报数据的跨链机制
        auditManager.setMechanism(requestId, "1");

        UniResponse response = new UniResponse(200,"success","");

        //拆分源、目标链请求
        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        CommonChainRequest desChainRequest = request.getDesChainRequest();

        //转出，转入，hash，锁定资金，锁定时间
        String srcChainRequestArgs = srcChainRequest.getArgs();
        String[] srcArgs = srcChainRequestArgs.split(",");

        //process start
        //初始化init用于标记
        String srcLockAddr = "";
        String srcSender = srcArgs[0];
        String srcHash = srcArgs[2];

        //上报的事务数据初始化
        TransactionAudit transAuditInfo = new TransactionAudit();

        //更新源链锁定参数，add current timestamp，当前系统的时间是底层区块链合约需要的参数
        long current_time = System.currentTimeMillis();
        srcChainRequest.setArgs(srcChainRequestArgs + "," + current_time);

        try {
            //step1:源链锁资产
            srcLockAddr = processLock(srcChainRequest, requestId);

            //step2: 目标链执行合约
            String desStatus = processDes(desChainRequest, requestId);

            String res = "";
            //判断目标链执行是否成功，若失败，则回滚
            if (CrossChainUtils.extractStatusField(desStatus).equals("1")) {
                //status==1 目标链合约执行结果表示成功，接下来执行unlock
                res = processUnlock(srcChainRequest, requestId, srcSender, srcHash, srcLockAddr);
            } else {
                //step3.2: status!=1 目标链合约执行结果表示失败，接下来执行rollback
                rollback(srcChainRequest, requestId, srcSender, srcHash);
            }

            //流程结束后组装事务数据
            CrossChainUtils.constructAuditInfo(transAuditInfo, groupManager, auditManager, request, res);
        } catch (LockException lockException) {
            //这里是锁定阶段出了错误，也就是说资产没有锁定，所以就不能回滚
            CrossChainUtils.constructErrorAuditInfo(transAuditInfo, request, groupManager, auditManager);
        } catch (Exception e) {
            //除了锁定错误外，其他出错都要回滚
            rollback(srcChainRequest, requestId, srcSender, srcHash);
            CrossChainUtils.constructErrorAuditInfo(transAuditInfo, request, groupManager, auditManager);
        } finally {
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }

        return response;
    }
}