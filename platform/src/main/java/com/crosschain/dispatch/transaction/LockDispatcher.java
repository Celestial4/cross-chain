package com.crosschain.dispatch.transaction;

import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.Group;
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
    String doDes(CrossChainRequest request, Group grp) throws Exception {
        CommonCrossChainRequest req = request.getDesChainRequest();
        check(grp, req);
        Pattern p = Pattern.compile("(\\w+,){4}(\\d+$)");
        String origin = req.getArgs();
        Matcher m = p.matcher(origin);
        if (m.find()) {
            String origin_dl = m.group(2);
            origin = origin.replaceAll(origin_dl, String.valueOf(src_deadline / 2));
        } else {
            origin += "\r\n" + src_deadline / 2;
        }
        req.setArgs(origin);
        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        //call des
        return lock_part(socAddress, req);
    }

    @Override
    String doSrc(CrossChainRequest request, Group grp) throws Exception {
        CommonCrossChainRequest req = request.getSrcChainRequest();
        check(grp, req);
        Pattern p = Pattern.compile("(\\w+,){4}(\\d+$)");
        Matcher m = p.matcher(req.getArgs());
        if (m.find()) {
            src_deadline = Long.parseLong(m.group(5));
        } else {
            throw new Exception("哈希时间锁参数设置错误");
        }
        String socAddress2 = systemInfo.getServiceAddr(req.getChainName());
        return lock_part(socAddress2, req);
    }

    private String lock_part(String socAddress, CommonCrossChainRequest req) throws Exception {
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);
        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);

        log.debug("received from blockchain:{}", res);

        String REGEX = "addr:(\\w*)";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(res);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new Exception(req.getChainName() + "锁定失败");
    }
}