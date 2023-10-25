package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.LockException;
import com.crosschain.service.response.UniResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 双边哈希时间锁
 */
@Slf4j
public class DualTransactionDispatcher extends HTLCBaseDispatcher {
    @Override
    public UniResponse process(CrossChainRequest request) throws Exception {
        //上报的事务数据初始化
        TransactionAudit transAuditInfo = new TransactionAudit();

        String requestId = request.getRequestId();

        //设置上报数据的跨链机制
        auditManager.setMechanism(requestId, "1");
        UniResponse response = new UniResponse(200, "success", "");

        //初始化init用于标记
        String srcLockAddr = "init";
        String desLockAddr = "init";
        String srcSender = "", desSender = "";
        String srcHash = "", desHash = "";

        //拆分源、目标链请求
        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        CommonChainRequest desChainRequest = request.getDesChainRequest();
        //转出，转入，hash，锁定资金，锁定时间
        String srcChainRequestArgs = srcChainRequest.getArgs();
        String desChainRequestArgs = desChainRequest.getArgs();
        String[] srcArgs = srcChainRequestArgs.split(",");
        String[] desArgs = desChainRequestArgs.split(",");

        //process start

        srcSender = srcArgs[0];
        desSender = desArgs[0];
        srcHash = srcArgs[2];
        desHash = srcHash;


        //更新源链锁定参数，add current timestamp，当前系统的时间是底层区块链合约需要的参数
        long current_time = System.currentTimeMillis();
        srcChainRequest.setArgs(srcChainRequestArgs + "," + current_time);

        //更新目标链锁定参数
        int desLockDuration = Integer.parseInt(srcArgs[4]) / 2;
        StringBuilder desLockArgsBuilder = new StringBuilder();
        for (int i = 0; i < desArgs.length; i++) {
            if (i == 2) {
                //hash
                desLockArgsBuilder.append(srcArgs[2]).append(",");
            }
            desLockArgsBuilder.append(desArgs[i]).append(",");
        }
        //目标链锁定时间,current_time
        desLockArgsBuilder.append(desLockDuration).append(",").append(current_time);
        desChainRequest.setArgs(desLockArgsBuilder.toString());

        try {
            //step1:锁资产
            srcLockAddr = processLock(srcChainRequest, requestId);
            desLockAddr = processLock(desChainRequest, requestId);

            //step2:解锁资产
            processUnlock(desChainRequest, requestId, desSender, desHash, desLockAddr);
            String res = processUnlock(srcChainRequest, requestId, srcSender, srcHash, srcLockAddr);

            //流程结束后组装事务数据
            CrossChainUtils.constructAuditInfo(transAuditInfo, groupManager, auditManager, request, res);
        } catch (LockException lockException) {
            //这里是锁定阶段出了错误，也就是说资产没有锁定，所以就不能回滚
            CrossChainUtils.constructErrorAuditInfo(transAuditInfo, request, groupManager, auditManager);
            if (!"init".equals(srcLockAddr)) {
                //源链已经锁定成功，需要回滚
                rollback(srcChainRequest, requestId, srcSender, srcHash);
            }
            if (!"init".equals(desLockAddr)) {
                //目标链链已经锁定成功，需要回滚
                rollback(desChainRequest, requestId, desSender, desHash);
            }
            response.setData("failed" + lockException.getErrorMsg());
        } catch (Exception e) {
            //除了锁定错误外，其他出错都要回滚
            //step3. 回滚资产
            rollback(srcChainRequest, requestId, srcSender, srcHash);
            rollback(desChainRequest, requestId, desSender, desHash);
            CrossChainUtils.constructErrorAuditInfo(transAuditInfo, request, groupManager, auditManager);
            response.setData("failed" + e.getMessage());
        } finally {
            auditManager.addTransactionInfo(requestId, transAuditInfo);
            auditManager.uploadAuditInfo(requestId);
        }

        return response;
    }
}