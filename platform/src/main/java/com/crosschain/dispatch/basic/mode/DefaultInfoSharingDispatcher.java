package com.crosschain.dispatch.basic.mode;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.audit.entity.TransactionAudit;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.basic.InfoSharingDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DefaultInfoSharingDispatcher extends InfoSharingDispatcher {

    @Override
    protected CommonChainResponse processDes(CommonChainRequest req, String req_id) throws
            Exception {

        log.info("[dest call info]:\n");
        //向目标链发起调用
        String res = sendTransaction(req);

        Chain chain = groupManager.getChain(req.getChainName());

        //做的是 数据上报的工作

        //组装区块链返回中的  扩展信息
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);

        //组装跨链的过程信息
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "call dest chain");

        //添加过程信息到总的数据上报结构体里
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));

        return new CommonChainResponse(res);
    }

    @Override
    protected CommonChainResponse processSrc(CommonChainRequest req, String req_id) throws Exception {

        log.info("[src call info]\n");
        String res = sendTransaction(req);

        Chain chain = groupManager.getChain(req.getChainName());
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(res);
        ProcessLog processLog = AuditUtils.buildProcessLog(chain, res, "call src chain");
        auditManager.addProcess(req_id, new ProcessAudit(res, processLog, extensionInfo));

        return new CommonChainResponse(res);
    }

    @Override
    protected String processResult(CommonChainResponse rep) {
        return "";
    }

    @Override
    protected void processAudit(TransactionAudit audit,
                                CrossChainRequest req,
                                String processResult,
                                String status) throws Exception {
        String req_id = req.getRequestId();

        Group group = groupManager.getGroup(req.getGroup());
        //跨链群组和网关id
        String grp_name = group.getGroupName();
        String gateway_id = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName()) + "," + SystemInfo.getGatewayAddr(req.getDesChainRequest().getChainName());
        audit.setChannel_name(grp_name);
        audit.setGateway_ids(gateway_id);

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

        //源链目标链信息
        audit.setSource_app_chain_type(src_chain_type);
        audit.setSource_app_chain_contract(src_contract);
        audit.setSource_app_chain_id(src_chain_id);
        audit.setSource_app_chain_service(src_chain_name);
        audit.setTarget_app_chain_type(des_chain_type);
        audit.setTarget_app_chain_contract(des_contract);
        audit.setTarget_app_chain_id(des_chain_id);
        audit.setTarget_app_chain_service(des_chain_name);

        //用户名和id
        String request_user_name = auditManager.getRequestUser(req_id);
        String request_user_id = CrossChainUtils.hash(request_user_name.getBytes(StandardCharsets.UTF_8));
        audit.setRequest_user(request_user_name);
        audit.setRequest_user_id(request_user_id);

        audit.setAction("1");

        String dataHash = "";
        int volume = 0;
        String behaviorContent = "";
        String behavioralResults = "";
        if ("1".equals(status)) {
            dataHash = CrossChainUtils.hash(processResult.getBytes(StandardCharsets.UTF_8));
            volume = processResult.getBytes(StandardCharsets.UTF_8).length / 8;
            behaviorContent = status;
            behavioralResults = status;
        }

        audit.setData_hash(dataHash);
        audit.setVolume(volume);
        audit.setBehavior_content(behaviorContent);
        audit.setBehavioral_results(behavioralResults);

        //设置status字段
        try {
            Pattern p = Pattern.compile("(status\":\\s*)(\"?)(\\w+)\\2");
            Matcher m = p.matcher(processResult);
            while (m.find()) {
                if (m.group(3).equals("2")) {
                    audit.setStatus(2);
                    break;
                } else {
                    audit.setStatus(1);
                }
            }
        } catch (Exception e) {
            audit.setStatus(2);
        }
    }
}