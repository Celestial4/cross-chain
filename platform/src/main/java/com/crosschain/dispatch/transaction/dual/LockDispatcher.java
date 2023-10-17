package com.crosschain.dispatch.transaction.dual;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.HTLCMechanismInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.ArgsResolveException;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.ResolveException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LockDispatcher extends TransactionBase {
    private long src_deadline = 0L;
    private long current_time = 0L;

    @Override
    protected String doDes(CommonChainRequest req, Group grp) throws Exception {
        checkAvailable0(grp, req);

        //读取源链设置的时间
        Pattern p = Pattern.compile("(\\w+,){4}(\\d+$)");
        String origin = req.getArgs();
        Matcher m = p.matcher(origin);
        //设置目标链的哈希终止时间为源链的一半
        if (m.find()) {
            String origin_dl = m.group(2);
            //不管目标链的锁定时间参数如何设置，这里程序将它重新设置为源链锁定时间的一半
            origin = origin.replaceAll(origin_dl, String.valueOf(src_deadline / 2));
        } else {
            origin += "," + src_deadline / 2;
        }
        //设置当前系统时间（也是区块链合约要求的，意义不明）
        origin += "," + current_time;
        req.setArgs(origin);

        //call des
        log.info("[src chain do]:\n");
        return lock_part(req);
    }

    @Override
    protected String doSrc(CommonChainRequest req, Group grp) throws Exception {

        //检查群组和请求合法性
        checkAvailable0(grp, req);

        Pattern p = Pattern.compile("(\\w+,){4}(\\d+)$");
        Matcher m = p.matcher(req.getArgs());
        //记录源链设置的哈希锁定终止时间
        if (m.find()) {
            src_deadline = Long.parseLong(m.group(5));
        } else {
            throw new ArgsResolveException("哈希时间");
        }

        //add current timestamp，这个是区块链合约要求的参数（意义不明）
        current_time = System.currentTimeMillis() / 1000;
        String ori = req.getArgs();
        ori = ori + "," + current_time;
        req.setArgs(ori);

        log.info("[src chain do]:\n");
        return lock_part(req);
    }

    @Override
    protected String getRollbackArgs(CommonChainRequest req) throws Exception {
        Pattern p = Pattern.compile("(\\w+)(,)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
        Matcher m = p.matcher(req.getArgs());
        String sender;
        String h;
        if (m.find()) {
            sender = m.group(1);
            h = m.group(4);
        } else {
            throw new ArgsResolveException("发送者和哈希原像");
        }
        return sender + "," + h;
    }

    @Override
    protected void processLast(CrossChainRequest req, String unlockResult) throws Exception {
        //上报lock阶段的Audit数据
        //auditManager.uploadAuditInfo(xxx);
    }

    private String lock_part(CommonChainRequest req) throws Exception {

        //调用区块链合约
        String res = sendTransaction(req);

        //处理跨链流程中的log或者过程信息
        Chain chain = groupManager.getChain(req.getChainName());
        String req_id = req.getRequestId();
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

        //判断锁是否成功，失败抛异常，后续回滚
        boolean status = extractInfo("status", res).equals("1");
        if (!status) {
            throw new CrossChainException(104, req.getChainName() + "资产锁定失败");
        }

        //组装跟踪回滚参数（用于后续需要回滚的情况） sender,hash,addr
        String unlock_args = getRollbackArgs(req);

        String REGEX = "addr:(\\w*)";
        p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(res);
        if (matcher.find()) {
            unlock_args += "," + matcher.group(1);
            return unlock_args;
        }

        throw new ResolveException("addr");
    }
}