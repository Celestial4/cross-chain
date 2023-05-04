package com.crosschain.dispatch.transaction.single;

import com.crosschain.audit.TransactionAudit;
import com.crosschain.common.*;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SingleTransactionCrossChainDispatcher extends BaseDispatcher {

    CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);

        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[dest chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        return new CommonCrossChainResponse(res);
    }

    CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);

        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        return new CommonCrossChainResponse(res);
    }

    void processAudit(CrossChainRequest req, String msgRtd) throws Exception {
        if (!req.getSrcChainRequest().getChainName().equals("hyperchain")) {
            return;
        }

        String proof, timestamp, action, status;

        Pattern p = Pattern.compile("[\\w\\s\":.,]*(?<=hash\":\")(\\w+)[\\w\\s\":.,]*(?<=time\":\")(\\w+)[\\w\\s\":.,]*(?<=action\":)(\\w+)[\\w\\s\":.,]*(?<=status\":)(\\w+)");
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

        String transaction_id_ingredient = auditManager.getRequestIngredient() + timestamp;
        MessageDigest digest = MessageDigest.getInstance("sha-256");
        digest.update(transaction_id_ingredient.getBytes(StandardCharsets.UTF_8));
        StringBuilder transaction_id = new StringBuilder();
        byte[] bytes = digest.digest();
        for (byte b : bytes) {
            transaction_id.append(String.format("%x", b));
        }

        TransactionAudit payload = new TransactionAudit(Integer.parseInt(action), grp_id, grp_name, gateway_id, user_name, src_contract, src_chain_id, Integer.parseInt(status), des_contract, des_chain_id, transaction_id.toString(), proof, receipt, time);

        auditManager.uploadAuditInfo(payload);
    }

    @Override
    public ResponseEntity process(CrossChainRequest request) throws Exception {
        setLocalChain(request);
        Group group = groupManager.getGroup(request.getGroup());
        log.info("[current group info]: {}", group.toString());
        ResponseEntity response = new ResponseEntity();
        if (group.getStatus() == 0) {

            CommonCrossChainRequest srcChainRequest = request.getSrcChainRequest();
            //add current timestamp
            long current_time = System.currentTimeMillis() / 1000;
            String ori = srcChainRequest.getArgs();
            ori = ori + "\r\n" + current_time;
            srcChainRequest.setArgs(ori);

            //源链锁资产
            CommonCrossChainResponse srcRes = processSrc(srcChainRequest, group);
            response.setSrcResult("lock:\n" + srcRes.getResult() + "\n");
            boolean res_flag = continueOrNot(srcRes);
            if (!res_flag) {
                throw new Exception("源链资产锁定失败");
            }

            Pattern p = Pattern.compile("(\\w+)(\\r\\n)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
            Matcher m = p.matcher(srcChainRequest.getArgs());
            String sender = null;
            String h = null;
            if (m.find()) {
                sender = m.group(1);
                h = m.group(4);
            } else {
                throw new Exception("源链事务合约参数设置错误");
            }

            //do deschain
            CommonCrossChainResponse DesRes = processDes(request.getDesChainRequest(), group);
            response.setDesResult(DesRes.getResult());

            //目标链执行成功
            res_flag = continueOrNot(DesRes);
            if (res_flag) {
                //unlock
                p = Pattern.compile("[\\w\\s\":.,]*(?<=addr\":\")(\\w+)");
                m = p.matcher(srcRes.getResult());
                if (m.find()) {
                    String lock_addr = m.group(1);
                    String unlock_args = sender + "\r\n" + h + "\r\n" + lock_addr;
                    srcChainRequest.setFunction("unlock");
                    srcChainRequest.setArgs(unlock_args);
                    srcRes = processSrc(srcChainRequest, group);
                    String final_src_resp = response.getSrcResult() + "unlock:\n" + srcRes.getResult();
                    response.setSrcResult(final_src_resp);
                } else {
                    throw new Exception("源链锁定合约返回结果中无锁定地址字段");
                }
            } else {
                //rollback
                String rollback_args = sender + "\r\n" + h;
                srcChainRequest.setFunction("rollback");
                srcChainRequest.setArgs(rollback_args);
                processSrc(srcChainRequest, group);
                srcRes.setResult("rollback");
                String final_src_resp = response.getSrcResult() + "rollbacked";
                response.setSrcResult(final_src_resp);
            }
            //源链解锁后上报事务数据
            if (!"rollback".equals(srcRes.getResult())) {
                processAudit(request, srcRes.getResult());
            }
            return response;
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }
    }
}