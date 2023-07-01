package com.crosschain.dispatch;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.CrossChainClient;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.exception.CrossChainException;
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
            throw new CrossChainException(103, String.format("%s链当前不可用,status=%d",reqInfo.getChainName(), chain.getStatus()));
        }

        log.info("group:{},{},chain:{},{}",grp.getGroupName(),grp.getStatus(),chain.getChainName(),chain.getStatus());
    }

    protected String extractInfo(String field, String source) throws Exception {
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

    protected String sendTransaction(CommonChainRequest req) throws Exception {
        try {
            String socAddress = SystemInfo.getServiceAddr(req.getChainName());
            String[] socketInfo = socAddress.split(":");
            log.info("[-----call info-----]\n[chain]:{}\n[contract]:{}\n[function]:{}\n[args]:{}\n[connection]:{}", req.getChainName(), req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

            byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs().replaceAll(",", "\r\n")});

            String res = new String(data, StandardCharsets.UTF_8);
            log.info("received from blockchain:{},{}", req.getChainName(), res);

            return res;
        } catch (Exception e) {
            throw new CrossChainException(500,String.format("合约%s执行异常：%s",req.getChainName(),e.getMessage()));
        }
    }

//    protected void processAudit(CrossChainRequest req, String msgRtd) throws Exception {
//        String proof, timestamp, action, status;
//
//        proof = extractInfo("hash", msgRtd);
//        timestamp = extractInfo("time", msgRtd);
//        action = extractInfo("action", msgRtd);
//        status = extractInfo("status", msgRtd);
//
//        String time = new Date(Long.parseLong(timestamp)).toString();
//        String receipt = "1".equals(status) ? "成功" : "失败";
//
//        Group group = groupManager.getGroup(req.getGroup());
//        String grp_name = group.getGroupName();
//        String grp_id = group.getGroupId();
//        String gateway_id = "gateway-" + SystemInfo.getSelfChainName();
//
//        Chain sChain = group.getChain(req.getSrcChainRequest().getChainName());
//        String src_chain_id = sChain.getChainId();
//        String src_contract = req.getSrcChainRequest().getContract();
//        String src_chain_name = sChain.getChainName();
//
//        Chain dChain = group.getChain(req.getDesChainRequest().getChainName());
//        String des_chain_id = dChain.getChainId();
//        String des_contract = req.getDesChainRequest().getContract();
//        String des_chain_name = dChain.getChainName();
//
//
//        String transaction_id_ingredient = auditManager.getRequestIngredient() + timestamp;
//        MessageDigest digest = MessageDigest.getInstance("sha-256");
//        digest.update(transaction_id_ingredient.getBytes(StandardCharsets.UTF_8));
//        StringBuilder transaction_id = new StringBuilder();
//        byte[] bytes = digest.digest();
//        for (byte b : bytes) {
//            transaction_id.append(String.format("%x", b));
//        }
//
//        String request_user_name = auditManager.getRequestUser();
//        String request_user_id = CrossChainUtils.hash(request_user_name.getBytes(StandardCharsets.UTF_8));
//        String target_user_name = auditManager.getTargetUser();
//        String target_user_id = CrossChainUtils.hash(target_user_name.getBytes(StandardCharsets.UTF_8));
//
//        String dataHash = CrossChainUtils.hash(msgRtd.getBytes(StandardCharsets.UTF_8));
//        int volume = msgRtd.getBytes(StandardCharsets.UTF_8).length / 8;
//        String behaviorContent = action;
//        String behavioralResults = status;
//
//        TransactionAudit payload = null;
//        auditManager.uploadAuditInfo(null);
//    }
}