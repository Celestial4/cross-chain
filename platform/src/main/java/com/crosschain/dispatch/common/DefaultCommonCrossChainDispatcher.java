package com.crosschain.dispatch.common;

import com.crosschain.audit.TransactionAudit;
import com.crosschain.common.*;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DefaultCommonCrossChainDispatcher extends CommonCrossChainDispatcherBase {

    @Override
    CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws
            Exception {
        Chain desChain = group.getChain(req.getChainName());
        if (!Objects.nonNull(desChain)) {
            log.debug("目标链不在跨链群组中");
            throw new Exception("目标链不在跨链群组");
        } else if (desChain.getStatus() != 0) {
            log.debug("目标链当前不可用");
            throw new Exception("目标链当前不可用");
        }
        log.info("[chain info]: {},{}", desChain.getChainName(), desChain.getStatus() == 0 ? "active" : "unavailable");
        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[dest chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);
        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception {
        Chain srcChain = group.getChain(req.getChainName());
        String socAddress = systemInfo.getServiceAddr(req.getChainName());

        if (!Objects.nonNull(srcChain)) {
            log.info("目标链不在跨链群组中");
            throw new Exception("目标链不在跨链群组");
        } else if (srcChain.getStatus() != 0) {
            log.info("目标链当前不可用");
            throw new Exception("目标链当前不可用");
        }
        log.info("[chain info]: {},{}", srcChain.getChainName(), srcChain.getStatus() == 0 ? "active" : "unavailable");
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);
        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);

        log.debug("received from blockchain:{}", res);
        return new CommonCrossChainResponse(res);
    }

    @Override
    String processResult(CommonCrossChainResponse rep) {
        //todo 此处实现对目标链的返回结果处理
        return "";
    }

    @Override
    void processAudit(CrossChainRequest req, String msgRtd) throws Exception {
        String proof, timestamp, action, status;

        Pattern p = Pattern.compile("[\\w\\s\":,]*(?<=hash\":\")(\\w+)[\\w\\s\":,]*(?<=time\":\")(\\w+)[\\w\\s\":,]*(?<=action\":)(\\w+)[\\w\\s\":,]*(?<=status\":)(\\w+)");
        Matcher m = p.matcher(msgRtd);
        if (m.find()) {
            proof = m.group(1);
            timestamp = m.group(2);
            action = m.group(3);
            status = m.group(4);
        } else {
            throw new Exception("合约返回值中缺少事务上报数据字段");
        }

        String time = new Date(Long.parseLong(timestamp)).toString();
        String receipt = "1".equals(status) ? "成功" : "失败";

        Group group = groupManager.getGroup(req.getGroup());
        String grp_name = group.getGroupName();
        String grp_id = group.getGroupId();
        String gateway_id = "gateway-" + SystemInfo.getSelfChainName();
        Chain sChain = group.getChain(req.getSrcChainRequest().getChainName());
        String src_chain_id = sChain.getChainId();
        String src_contract = req.getSrcChainRequest().getContract();

        Chain dChain = group.getChain(req.getDesChainRequest().getChainName());
        String des_chain_id = dChain.getChainId();
        String des_contract = req.getDesChainRequest().getContract();
        String user_name = auditManager.getUserInfo();

        String transaction_id_ingredient = auditManager.getRequestIngredient()+timestamp;
        MessageDigest digest = MessageDigest.getInstance("sha-256");
        digest.update(transaction_id_ingredient.getBytes(StandardCharsets.UTF_8));
        StringBuilder transaction_id = new StringBuilder();
        byte[] bytes = digest.digest();
        for (byte b : bytes) {
            transaction_id.append(String.format("%x",b));
        }

        TransactionAudit payload = new TransactionAudit(Integer.parseInt(action), grp_id, grp_name, gateway_id, user_name, src_contract, src_chain_id, Integer.parseInt(status), des_contract, des_chain_id, transaction_id.toString(), proof, receipt, time);

        auditManager.uploadAuditInfo(payload);
    }
}