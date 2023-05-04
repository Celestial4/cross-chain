package com.crosschain.dispatch;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.*;
import com.crosschain.group.GroupManager;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseDispatcher implements Dispatcher {
    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    protected GroupManager groupManager;

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    protected SystemInfo systemInfo;

    public void setAuditManager(AuditManager auditManager) {
        this.auditManager = auditManager;
    }

    protected AuditManager auditManager;

    protected void setLocalChain(CrossChainRequest req) {
        CommonCrossChainRequest src = req.getSrcChainRequest();
        if (src.getChainName().equals("local")) {
            src.setChainName(SystemInfo.getSelfChainName());
        }

        CommonCrossChainRequest des = req.getDesChainRequest();
        if (des.getChainName().equals("local")) {
            des.setChainName(SystemInfo.getSelfChainName());
        }
    }

    protected void checkAvailable(Group grp, CommonCrossChainRequest reqInfo) throws Exception {
        Chain chain = grp.getChain(reqInfo.getChainName());

        if (!Objects.nonNull(chain)) {
            throw new Exception("target chain is not in crosschain group");
        } else if (chain.getStatus() != 0) {
            throw new Exception("target chain is not available now");
        }
    }

    //判断链调用结果是否成功或者失败，1成功，2失败
    protected boolean continueOrNot(CommonCrossChainResponse rep) throws Exception{
        Pattern p = Pattern.compile("[\\w\\s\":.,]*(?<=status\":\")(\\w+)");
        Matcher m = p.matcher(rep.getResult());
        if (m.find()) {
            return m.group(1).equals("1");
        } else {
            throw new Exception("无法判断合约执行情况，合约结果中未找到status字段");
        }
    }
}