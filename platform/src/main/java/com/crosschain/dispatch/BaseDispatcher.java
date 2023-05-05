package com.crosschain.dispatch;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.*;
import com.crosschain.group.GroupManager;
import com.crosschain.service.response.Response;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //判断链调用结果是否成功或者失败，1成功，2失败
    protected boolean continueOrNot(CommonChainResponse rep) throws Exception {
        Pattern p = Pattern.compile("(?<=status\":\\s?\"?)(\\w+)");
        Matcher m = p.matcher(rep.getResult());
        if (m.find()) {
            return m.group(1).equals("1");
        } else {
            throw new Exception("无法判断合约执行情况，合约结果中未找到status字段");
        }
    }

    protected String extractInfo(String field, String source) throws Exception {
        Pattern p = Pattern.compile(String.format("(?<=%s\"?:\\s?\"?)(\\w+)", field));
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group();
        } else {
            throw new Exception("合约返回值中没有找到" + field + "字段");
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
}