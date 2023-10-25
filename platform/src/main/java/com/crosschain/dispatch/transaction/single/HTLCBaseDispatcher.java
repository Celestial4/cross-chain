package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.HTLCMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.exception.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTLCBaseDispatcher extends BaseDispatcher {
    /**
     * 通用锁定
     *
     * @param request   通用跨链请求
     * @param requestId 一次跨链请求id
     * @return 锁定地址
     * @throws UniException 锁定异常信息
     */
    protected String processLock(CommonChainRequest request, String requestId) throws UniException {
        String lockAddr = "";
        try {
            String chainName = request.getChainName();
            log.info("[chain:{} do lock]", chainName);
            String res = "";

            //通过BaseDispatcher 发起链合约调用
            res = sendTransaction(request);
            Chain chain = groupManager.getChain(chainName);

            //处理跨链流程中的log或者过程信息
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, String.format("chain:[%s] do lock", chainName));
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
            auditManager.addProcess(requestId, new ProcessAudit(res, processLog, extensionInfo));

            //处理哈希时间锁机制的过程信息
            //从请求参数中可以直接解析出来锁定资金和锁定时间的参数
            String[] args = request.getArgs().split(",");
            auditManager.addHTLCInfo(requestId, new HTLCMechanismInfo(String.format("chain:[%s],lockAmount:%s", chainName, args[3]), "", args[4]));

            //根据锁定函数返回的结果判断锁定是否成功
            if (!CrossChainUtils.extractStatusField(res).equals("1")) {
                auditManager.addHTLCInfo(requestId, new HTLCMechanismInfo(String.format("chain:[%s],lockAmount:%s", chainName, args[3]), "", "lock failed."));
                throw new CrossChainException(104, String.format("chain:[%s]资产锁定失败,请检查跨链参数或相应区块链，详情：%s", chainName, CrossChainUtils.extractInfo("data", res)));
            } else {
                lockAddr = CrossChainUtils.extractInfo("addr", res);
            }
        } catch (Exception e) {
            log.debug("exception occurs at:" + CrossChainUtils.getErrorStackInfo(e) + "\n" + e.getMessage());
            throw new LockException(e.getMessage());
        }

        return lockAddr;
    }

    /**
     * 通用解锁
     *
     * @param request   通用跨链请求
     * @param requestId 跨链请求id
     * @param sender    资产转移中的出方
     * @param hash      哈希原像
     * @param lockAddr  锁定地址
     * @return 解锁合约的返回结果
     * @throws UniException 解锁异常信息
     */
    protected String processUnlock(CommonChainRequest request, String requestId, String sender, String hash, String lockAddr) throws UniException {
        String res = "";
        try {
            //更新链调用对象
            String unlock_args = sender + "," + hash + "," + lockAddr + "," + System.currentTimeMillis();
            request.setArgs(unlock_args);
            request.setFunction("unlock");

            String chainName = request.getChainName();
            log.info("[chain:{} do unlock]", chainName);
            res = sendTransaction(request);

            //设置unlock过程信息
            Chain chain = groupManager.getChain(chainName);
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, String.format("chain:[%s] do unlock", chainName));
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
            auditManager.addProcess(requestId, new ProcessAudit(res, processLog, extensionInfo));

            //判断源链解锁是否成功，若失败，则抛异常，后续回滚
            if (!CrossChainUtils.extractStatusField(res).equals("1")) {
                throw new CrossChainException(105, String.format("chain:[%s]资产解锁失败,详情：%s", chainName, CrossChainUtils.extractInfo("data", res)));
            }
        } catch (Exception e) {
            log.debug("exception occurs at:" + CrossChainUtils.getErrorStackInfo(e) + "\n" + e.getMessage());
            throw new ProcessException(e.getMessage());
        }
        return res;
    }

    /**
     * 通用回滚
     *
     * @param request   通用跨链请求
     * @param requestId 跨链请求id
     * @param sender    资产转移中的出方
     * @param hash      哈希原像
     * @throws UniException 回滚异常信息
     */
    protected void rollback(CommonChainRequest request, String requestId, String sender, String hash) throws UniException {

        try {
            String chainName = request.getChainName();
            log.info("[chain:{} do rollback]", chainName);
            String rollback_args = sender + "," + hash;
            request.setFunction("rollback");
            request.setArgs(rollback_args);

            String res = sendTransaction(request);

            Chain chain = groupManager.getChain(chainName);
            ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, String.format("chain:[%s] do rollback", chainName));
            ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
            auditManager.addProcess(requestId, new ProcessAudit(res, processLog, extensionInfo));
            for (HTLCMechanismInfo htlcMechanismInfo : auditManager.getHTLCInfo(requestId)) {
                if (htlcMechanismInfo.getHtlc_lock().contains("[" + chainName + "]")) {
                    htlcMechanismInfo.setHtlc_status(String.format("chain:[%s] 状态回滚，不解锁", chainName));
                }
            }
        } catch (Exception e) {
            log.debug("exception occurs at:" + CrossChainUtils.getErrorStackInfo(e) + "\n" + e.getMessage());
            throw new ProcessException(e.getMessage());
        }
    }
}