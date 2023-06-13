package com.crosschain.dispatch;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.*;
import com.crosschain.group.GroupManager;
import com.crosschain.service.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BaseDispatcher implements Dispatcher {
    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    protected GroupManager groupManager;

    public void setAuditManager(AuditManager auditManager) {
        this.auditManager = auditManager;
    }

    protected AuditManager auditManager;

    protected void setLocalChain(CrossChainRequest req) {
        CommonChainRequest src = req.getSrcChainRequest();
        if (src.getChainName().equals("local")) {
            src.setChainName(SystemInfo.getSelfChainName());
        }

        CommonChainRequest des = req.getDesChainRequest();
        if (des.getChainName().equals("local")) {
            des.setChainName(SystemInfo.getSelfChainName());
        }
    }

    protected void checkAvailable(Group grp, CommonChainRequest reqInfo) throws Exception {
        Chain chain = grp.getChain(reqInfo.getChainName());

        if (!Objects.nonNull(chain)) {
            throw new Exception("target chain is not in crosschain group");
        } else if (chain.getStatus() != 0) {
            throw new Exception("target chain is not available now");
        }
    }

    protected String extractInfo(String field, String source) throws Exception {
        Pattern p = Pattern.compile(String.format("(?<=%s\"?:\\s?\"?)(\\w+)", field));
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group();
        } else {
            throw new Exception("合约返回值中不能解析出" + field + "字段");
        }
    }

    @Override
    public Response process(CrossChainRequest req) throws Exception {
        //父类实现不做任何事情
        return null;
    }

    @Override
    public Response process(CommonChainRequest req) throws Exception {
        //父类实现不做任何事情
        return null;
    }

    protected String sendTransaction(CommonChainRequest req) throws Exception {
        String socAddress = SystemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("-----call info-----\n[chain]:{}\n[contract]:{}\n[function]:{}\n[args]:{}\n[connection]:{}", req.getChainName(), req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.info("received from blockchain:{}\n{}", req.getChainName(), res);

        return res;
    }
}