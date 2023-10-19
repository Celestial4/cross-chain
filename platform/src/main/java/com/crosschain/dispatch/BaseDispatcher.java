package com.crosschain.dispatch;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.CrossChainClient;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.UniException;
import com.crosschain.group.GroupManager;
import com.crosschain.service.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
public class BaseDispatcher implements Dispatcher {

    protected Integer CrossChainMechanism;

    protected AuditManager auditManager;

    protected GroupManager groupManager;

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public void setCrossChainMechanism(Integer crossChainMechanism) {
        CrossChainMechanism = crossChainMechanism;
    }

    public void setAuditManager(AuditManager auditManager) {
        this.auditManager = auditManager;
    }

    protected void checkAvailable0(Group grp, CommonChainRequest reqInfo) throws Exception {
        if (grp.getStatus() != 0) {
            throw new CrossChainException(101, String.format("跨链群组当前不可用,status=%d", grp.getStatus()));
        }

        Chain chain = grp.getChain(reqInfo.getChainName());

        if (Objects.isNull(chain)) {
            throw new CrossChainException(102, String.format("%s链不在跨链群组中，请先联系跨链管理员", reqInfo.getChainName()));
        } else if (chain.getStatus() != 0) {
            throw new CrossChainException(103, String.format("%s链当前不可用,status=%d", reqInfo.getChainName(), chain.getStatus()));
        }

        log.info("group:{},{},chain:{},{}", grp.getGroupName(), grp.getStatus(), chain.getChainName(), chain.getStatus());
    }

    protected String extractInfo(String field, String source) throws UniException {
        return CrossChainUtils.extractInfo(field, source);
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

    @Override
    public void checkAvailable(Group grp, List<CommonChainRequest> reqs) throws Exception {
        for (CommonChainRequest req : reqs) {
            checkAvailable0(grp, req);
        }
    }

    @Override
    public void completeTask(String id) {
        auditManager.completeRequest(id);
    }

    /**
     * 向跨链服务组件发送具体的 链执行信息
     *
     * @param req 单条链的执行信息
     * @return 链执行的结果信息
     * @throws Exception
     */
    protected String sendTransaction(CommonChainRequest req) throws Exception {
        try {
            //获取到要调的链的服务组件连接信息
            String socAddress = SystemInfo.getServiceAddr(req.getChainName());

            String[] socketInfo = socAddress.split(":");
            log.info("[-----call info-----]\n[chain]:{}\n[contract]:{}\n[function]:{}\n[args]:{}\n[connection]:{}", req.getChainName(), req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

            byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs().replaceAll(",", "\r\n"), req.getRequestId()});

            String res = new String(data, StandardCharsets.UTF_8);
            log.info("received from blockchain:{},{}", req.getChainName(), res);

            return res;
        } catch (Exception e) {
            throw new CrossChainException(500, String.format("链%s执行合约%s异常：%s", req.getChainName(), req.getContract(), e.getMessage()));
        }
    }
}