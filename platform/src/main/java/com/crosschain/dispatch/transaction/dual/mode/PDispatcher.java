package com.crosschain.dispatch.transaction.dual.mode;

import com.crosschain.audit.entity.DPKMechanismInfo;
import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.AuditUtils;
import com.crosschain.common.CrossChainUtils;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PDispatcher extends OtherDispatcherBase {
    public PDispatcher(String m) {
        mode = m;
    }

    @Override
    protected String getMechanism() {
        return "3";
    }

    @Override
    protected void setProcessInfo(String req_id, String result, CommonChainRequest req) {
        Chain chain = null;
        String errorInfo = "";
        try {
            try {
                errorInfo = CrossChainUtils.extractInfo("data", result);
            } catch (Exception e) {
                //donothing
            }
            chain = groupManager.getChain(req.getChainName());
            Map<Integer, String> dpkMap = new HashMap<>();

            Pattern p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,.:;\\s!]+)\\2", "dpky_id"));
            Matcher m = p.matcher(result);
            int cnt = 0;
            while (m.find()) {
                dpkMap.put(cnt++, m.group(3));
            }

            p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,.:;\\s!]+)\\2", "dpky_choice"));
            Matcher m1 = p.matcher(result);
            List<String> choices = new ArrayList<>();
            while (m1.find()) {
                choices.add(m1.group(3));
            }

            p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,.:;\\s!]+)\\2", "dpky_time"));
            Matcher m2 = p.matcher(result);
            List<String> times = new ArrayList<>();
            while (m2.find()) {
                times.add(m2.group(3));
            }

            for (int i = 0; i < cnt; i++) {

                String signer = "signer" + (i + 1);
                ProcessLog processLog = AuditUtils.buildProcessLog(chain, result, signer);
                ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(result);
                auditManager.addProcess(req_id, new ProcessAudit(result, processLog, extensionInfo));
            }
        } catch (Exception e) {
            log.debug("获取DPKY过程信息异常：" + e.getMessage());
            ProcessLog processLog = AuditUtils.buildErrorProcessLog(chain, result, "", errorInfo);
            auditManager.addProcess(req_id, new ProcessAudit("dpky occurs exception", processLog));
        }
    }

    @Override
    protected void addMechanismInfo(String requestId, String result) throws UniException {
        try {
            List<String> dpk_ids = new ArrayList<>();
            List<String> dpk_choices = new ArrayList<>();
            Pattern p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,.:;\\s!]+)\\2", "dpky_id"));
            Matcher m = p.matcher(result);
            int cnt = 0;
            while (m.find()) {
                cnt++;
                dpk_ids.add(m.group(3));
            }

            p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,.:;\\s!]+)\\2", "dpky_choice"));
            Matcher m1 = p.matcher(result);
            while (m1.find()) {
                dpk_choices.add(m1.group(3));
            }

            StringBuilder dpky_ids = new StringBuilder();
            StringBuilder dpkys = new StringBuilder();
            for (int i = 0; i < cnt; i++) {
                dpky_ids.append(dpk_ids.get(i)).append(i == cnt - 1 ? "" : ",");
                dpkys.append(dpk_choices.get(i)).append(i == cnt - 1 ? "" : ",");
            }

            String dpky_ip = SystemInfo.getGatewayAddr(SystemInfo.getSelfChainName());
            DPKMechanismInfo dpkMechanismInfo = new DPKMechanismInfo(dpky_ids.toString(), dpky_ip, dpkys.toString());
            auditManager.addDpkInfo(requestId, dpkMechanismInfo);
        } catch (Exception e) {
            log.debug("获取DPKY机制信息异常：" + e.getMessage());
            auditManager.addDpkInfo(requestId, new DPKMechanismInfo());
        }
    }

    @Override
    protected void finishCrosschain(String result) throws UniException {
        if (!CrossChainUtils.extractStatusField(result).equals("1")) {
            String errorInfo = CrossChainUtils.extractInfo("data", result);
            throw new CrossChainException(800, "DPKY跨链失败：" + errorInfo);
        }
    }
}