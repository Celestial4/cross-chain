package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LockDispatcher extends TransactionBase {
    private long src_deadline = 0L;

    @Override
    protected String doDes(CrossChainRequest request, Group grp) throws Exception {
        CommonChainRequest req = request.getDesChainRequest();
        checkAvailable(grp, req);

        Pattern p = Pattern.compile("(\\w+\r\n){4}(\\d+$)");
        String origin = req.getArgs();
        Matcher m = p.matcher(origin);
        if (m.find()) {
            String origin_dl = m.group(2);
            origin = origin.replaceAll(origin_dl, String.valueOf(src_deadline / 2));
        } else {
            origin += "\r\n" + src_deadline / 2;
        }
        req.setArgs(origin);
        String socAddress = SystemInfo.getServiceAddr(req.getChainName());

        //call des
        return lock_part(socAddress, req);
    }

    @Override
    protected String doSrc(CrossChainRequest request, Group grp) throws Exception {
        CommonChainRequest req = request.getSrcChainRequest();
        checkAvailable(grp, req);

        Pattern p = Pattern.compile("(\\w+\r\n){4}(\\d+$)");
        Matcher m = p.matcher(req.getArgs());
        if (m.find()) {
            src_deadline = Long.parseLong(m.group(5));
        } else {
            throw new Exception("哈希时间锁参数设置错误");
        }
        String socAddress2 = SystemInfo.getServiceAddr(req.getChainName());

        return lock_part(socAddress2, req);
    }

    private String lock_part(String socAddress, CommonChainRequest req) throws Exception {
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}\n{}", req.getChainName(),res);

        String REGEX = "addr:(\\w*)";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(res);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new Exception(req.getChainName() + "锁定失败");
    }
}