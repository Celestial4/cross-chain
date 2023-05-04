package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.Group;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UnlockDispatcher extends TransactionBase {
    private String h;

    @Override
    protected String doDes(CrossChainRequest request, Group grp) throws Exception {
        //actually do deschain
        CommonCrossChainRequest req = request.getSrcChainRequest();
        checkAvailable(grp, req);

        String origin = req.getArgs();
        Pattern p = Pattern.compile("(^\\w+)\r\n(\\w+$)");
        Matcher m = p.matcher(origin);
        String args = "";
        if (m.find()) {
            String first = m.group(1);
            String last = m.group(2);
            args = first + h + "," + last;
        }
        req.setArgs(args);

        String socketAddr = systemInfo.getServiceAddr(req.getChainName());

        //blockchain do unlock
        return unlock_part(socketAddr, req);
    }

    @Override
    protected String doSrc(CrossChainRequest request, Group grp) throws Exception {
        //actually do deschain
        CommonCrossChainRequest req = request.getDesChainRequest();
        checkAvailable(grp, req);

        String origin = req.getArgs();
        Pattern p = Pattern.compile("(?<=\\w\r\n)(\\w+)(?=\r\n\\w)");
        Matcher m = p.matcher(origin);
        if (m.find()) {
            h = m.group(1);
        } else {
            throw new Exception("没有哈希原像参数");
        }

        if (m.find()) {
            //有其他参数，表示非哈希事务合约
            int count = 0;
            while (count < 3) {
                origin = origin.substring(origin.indexOf(",")+1);
                count++;
            }
        }

        req.setArgs(origin);
        String socketAddr = systemInfo.getServiceAddr(req.getChainName());

        //blockchain do unlock
        return unlock_part(socketAddr, req);
    }

    private String unlock_part(String socAddress, CommonCrossChainRequest req) throws Exception {
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        String REGEX = "success";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(res);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new Exception(req.getChainName() + "解锁失败");
    }
}