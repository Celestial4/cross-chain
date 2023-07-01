package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.ArgsResolveException;
import com.crosschain.exception.CrossChainException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UnlockDispatcher extends TransactionBase {
    private long current_time;

    @Override
    protected String doDes(CommonChainRequest req, Group grp) throws Exception {

        checkAvailable0(grp, req);

        String ori = req.getArgs();
        ori = ori + "," + current_time;
        req.setArgs(ori);

        //blockchain do unlock
        return unlock_part(req);
    }

    @Override
    protected String doSrc(CommonChainRequest req, Group grp) throws Exception {
        checkAvailable0(grp, req);
        //add current timestamp
        current_time = System.currentTimeMillis() / 1000;
        String ori = req.getArgs();
        ori = ori + "," + current_time;
        req.setArgs(ori);
        //blockchain do unlock
        return unlock_part(req);
    }

    @Override
    protected String getRollbackArgs(CommonChainRequest req) throws Exception {
        Pattern p = Pattern.compile("^(\\w+),(\\w+),(\\w+)");
        Matcher m = p.matcher(req.getArgs());
        String sender;
        String h;
        if (m.find()) {
            sender = m.group(1);
            h = m.group(2);
        } else {
            throw new ArgsResolveException("发送者和哈希原像");
        }
        return sender + "," + h;
    }

    @Override
    protected void processLast(CrossChainRequest req, String unlockResult) throws Exception {

    }

    private String unlock_part(CommonChainRequest req) throws Exception {
        String res = sendTransaction(req);

        boolean status = extractInfo("status", res).equals("1");
        if (!status) {
            throw new CrossChainException(105, req.getChainName() + "资产解锁失败");
        }

        return res;
    }
}