package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
import com.crosschain.dispatch.CrossChainRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UnlockDispatcher extends TransactionBase {
    private String h;
    private long current_time;

    @Override
    protected String doDes(CommonChainRequest req, Group grp) throws Exception {

        checkAvailable(grp, req);

        String ori = req.getArgs();
        ori = ori + "\r\n" + current_time;
        req.setArgs(ori);

        //blockchain do unlock
        return unlock_part(req);
    }

    @Override
    protected String doSrc(CommonChainRequest req, Group grp) throws Exception {
        checkAvailable(grp, req);
        //add current timestamp
        current_time = System.currentTimeMillis() / 1000;
        String ori = req.getArgs();
        ori = ori + "\r\n" + current_time;
        req.setArgs(ori);
        //blockchain do unlock
        return unlock_part(req);
    }

    @Override
    protected String getRollbackArgs(CommonChainRequest req) throws Exception {
        Pattern p = Pattern.compile("^(\\w+)\\s+(\\w+)\\s+(\\w+)");
        Matcher m = p.matcher(req.getArgs());
        String sender;
        String h;
        if (m.find()) {
            sender = m.group(1);
            h = m.group(2);
        } else {
            throw new Exception("事务合约参数设置错误，请检查发送者和哈希原像参数");
        }
        return sender + "\r\n" + h;
    }

    @Override
    protected void processLast(CrossChainRequest req, String unlockResult) throws Exception {
        processAudit(req, unlockResult);
    }

    private String unlock_part(CommonChainRequest req) throws Exception {
        String res = sendTransaction(req);

        try {
            boolean status = extractInfo("status", res).equals("1");
            if (!status) {
                throw new Exception(req.getChainName() + "资产解锁失败");
            }
        } catch (Exception e) {
            throw new Exception("跨链失败：事务合约执行异常，" + e.getMessage());
        }

        throw new Exception(req.getChainName() + "解锁失败");
    }
}