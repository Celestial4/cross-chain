package com.crosschain.audit.entity;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.exception.ResolveException;
import com.crosschain.exception.UniException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TransactionAudit {
    String action = "";
    Integer status = 2;
    String channel_name = "";
    String gateway_ids = "";

    String request_user_id = "";
    String request_user = "";
    String target_user_id = "";
    String target_user = "";

    String source_app_chain_contract = "";
    String source_app_chain_id = "";
    String source_app_chain_service = "";
    String source_app_chain_type = "";
    String target_app_chain_contract = "";
    String target_app_chain_id = "";
    //链名
    String target_app_chain_service = "";
    //链类型
    String target_app_chain_type = "";

    String transaction_id = "";
    String transaction_proof = "";
    String transaction_receipt = "";
    String transaction_time = "";

    String data_hash = "";
    Integer volume;
    String behavior_content = "";
    String behavioral_results = "";
    List<Mechanism> mechanism_info = new ArrayList<>();

    public static void construct(TransactionAudit payload,
                                 Group group,
                                 AuditManager auditManager,
                                 CrossChainRequest req,
                                 String transactionRes) throws Exception {
        String proof, timestamp, action, status;
        String req_id = req.getRequestId();

        try {
            proof = CrossChainUtils.extractInfo("hash", transactionRes);
            timestamp = CrossChainUtils.extractInfo("time", transactionRes);
            action = CrossChainUtils.extractInfo("action", transactionRes);
            status = CrossChainUtils.extractStatusField(transactionRes);

        } catch (UniException e) {
            proof = "null";
            timestamp = "0";
            action = "";
            status = "2";
        }

        try {

            String time = timestamp;
            String receipt = "1".equals(status) ? "成功" : "失败";

            String grp_name = group.getGroupName();
            String gateway_id = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName()) + "," + SystemInfo.getGatewayAddr(req.getDesChainRequest().getChainName());

            Chain sChain = group.getChain(SystemInfo.getSelfChainName());
            String src_chain_id = sChain.getChainId();
            String src_contract = req.getSrcChainRequest().getContract();
            String src_chain_name = sChain.getChainName();
            String src_chain_type = sChain.getChainType();

            Chain dChain = group.getChain(req.getDesChainRequest().getChainName());
            String des_chain_id = dChain.getChainId();
            String des_contract = req.getDesChainRequest().getContract();
            String des_chain_name = dChain.getChainName();
            String des_chain_type = sChain.getChainType();


            String transaction_id_ingredient = auditManager.getRequestIngredient(req_id) + timestamp;
            MessageDigest digest = MessageDigest.getInstance("sha-256");
            digest.update(transaction_id_ingredient.getBytes(StandardCharsets.UTF_8));
            StringBuilder transaction_id = new StringBuilder();
            byte[] bytes = digest.digest();
            for (byte b : bytes) {
                transaction_id.append(String.format("%x", b));
            }

            String request_user_name = auditManager.getRequestUser(req_id);
            String request_user_id = CrossChainUtils.hash(request_user_name.getBytes(StandardCharsets.UTF_8));
            String target_user_name = auditManager.getTargetUser(req_id);
            String target_user_id = CrossChainUtils.hash(target_user_name.getBytes(StandardCharsets.UTF_8));

            String dataHash = CrossChainUtils.hash(transactionRes.getBytes(StandardCharsets.UTF_8));
            int volume = transactionRes.getBytes(StandardCharsets.UTF_8).length / 8;
            String behaviorContent = action;
            String behavioralResults = status;

            payload.setAction(action);

            payload.setStatus(Integer.parseInt(status));
            payload.setChannel_name(grp_name);
            payload.setGateway_ids(gateway_id);

            payload.setRequest_user(request_user_name);
            payload.setRequest_user_id(request_user_id);
            payload.setTarget_user(target_user_name);
            payload.setTarget_user_id(target_user_id);

            payload.setSource_app_chain_type(src_chain_type);
            payload.setSource_app_chain_contract(src_contract);
            payload.setSource_app_chain_id(src_chain_id);
            payload.setSource_app_chain_service(src_chain_name);
            payload.setTarget_app_chain_type(des_chain_type);
            payload.setTarget_app_chain_contract(des_contract);
            payload.setTarget_app_chain_id(des_chain_id);
            payload.setTarget_app_chain_service(des_chain_name);

            payload.setTransaction_id(transaction_id.toString());
            payload.setTransaction_time(time);
            payload.setTransaction_receipt(receipt);
            payload.setTransaction_proof(proof);

            payload.setData_hash(dataHash);
            payload.setVolume(volume);
            payload.setBehavior_content(behaviorContent);
            payload.setBehavioral_results(behavioralResults);
        } catch (Exception e) {
            log.debug(e.toString());
            throw new ResolveException("构造跨链事务数据异常：" + e.getMessage());
        }
    }

    public static void setErrorCallAuditInfo(TransactionAudit payload, CrossChainRequest req, Group group, AuditManager auditManager) throws Exception {

        Chain sChain = group.getChain(SystemInfo.getSelfChainName());
        String src_chain_id = sChain.getChainId();
        String src_contract = req.getSrcChainRequest().getContract();
        String src_chain_name = sChain.getChainName();
        String src_chain_type = sChain.getChainType();

        Chain dChain = group.getChain(req.getDesChainRequest().getChainName());
        String des_chain_id = dChain.getChainId();
        String des_contract = req.getDesChainRequest().getContract();
        String des_chain_name = dChain.getChainName();
        String des_chain_type = dChain.getChainType();

        payload.setSource_app_chain_type(src_chain_type);
        payload.setSource_app_chain_contract(src_contract);
        payload.setSource_app_chain_id(src_chain_id);
        payload.setSource_app_chain_service(src_chain_name);
        payload.setTarget_app_chain_type(des_chain_type);
        payload.setTarget_app_chain_contract(des_contract);
        payload.setTarget_app_chain_id(des_chain_id);
        payload.setTarget_app_chain_service(des_chain_name);

        String grp_name = group.getGroupName();
        String gateway_id = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName()) + "," + SystemInfo.getGatewayAddr(req.getDesChainRequest().getChainName());

        payload.setChannel_name(grp_name);
        payload.setGateway_ids(gateway_id);

        //用户名和id
        String request_user_name = auditManager.getRequestUser(req.getRequestId());
        String request_user_id = CrossChainUtils.hash(request_user_name.getBytes(StandardCharsets.UTF_8));
        payload.setRequest_user(request_user_name);
        payload.setRequest_user_id(request_user_id);
        payload.setStatus(2);
    }
}