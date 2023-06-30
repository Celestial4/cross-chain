package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
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
        checkAvailable(grp, req);

        //读取源链设置的时间
        Pattern p = Pattern.compile("(\\w+,){4}(\\d+$)");
        String origin = req.getArgs();
        Matcher m = p.matcher(origin);
        //设置目标链的哈希终止时间为源链的一半
        if (m.find()) {
            String origin_dl = m.group(2);
            origin = origin.replaceAll(origin_dl, String.valueOf(src_deadline / 2));
        } else {
            origin += "," + src_deadline / 2;
        }
        //设置当前系统时间
        origin += "," + current_time;
        req.setArgs(origin);

        //call des
        log.info("[src chain do]:\n");
        return lock_part(req);
    }

    @Override
    protected String doSrc(CommonChainRequest req, Group grp) throws Exception {

        checkAvailable(grp, req);

        Pattern p = Pattern.compile("(\\w+,){4}(\\d+)$");
        Matcher m = p.matcher(req.getArgs());
        //记录源链设置的哈希锁定终止时间
        if (m.find()) {
            src_deadline = Long.parseLong(m.group(5));
        } else {
            throw new ArgsResolveException("哈希时间");
        }

        //add current timestamp
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
        //do nothing in lock phase
    }

    private String lock_part(CommonChainRequest req) throws Exception {

        String res = sendTransaction(req);

        boolean status = extractInfo("status", res).equals("1");
        if (!status) {
            throw new CrossChainException(104, req.getChainName() + "资产锁定失败");
        }

        String unlock_args = getRollbackArgs(req);

        String REGEX = "addr:(\\w*)";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(res);
        if (matcher.find()) {
            unlock_args += "," + matcher.group(1);
            return unlock_args;
        }

        throw new ResolveException("addr");
    }
}