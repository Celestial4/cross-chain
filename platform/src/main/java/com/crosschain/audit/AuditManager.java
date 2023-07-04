package com.crosschain.audit;

import com.crosschain.audit.entity.*;
import com.crosschain.common.SystemInfo;
import com.crosschain.exception.CrossChainException;
import com.crosschain.exception.OperationException;
import com.crosschain.exception.UniException;
import com.crosschain.service.request.CrossChainVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AuditManager {

    private Map<String,CrossChainVo> voMap = new ConcurrentHashMap<>(1);

    private Map<String, FullAuditInfo> auditionMap = new ConcurrentHashMap<>(1);

    public void joinRequest(CrossChainVo request) throws UniException {
        String id = request.getRequest_id();
        if (!voMap.containsKey(id)) {
            voMap.put(id, request);
            FullAuditInfo ai = new FullAuditInfo();
            ai.setRequest_id(id);
            auditionMap.put(id, ai);
        } else {
            throw new OperationException(String.format("%s跨链请求已经存在", id));
        }
    }

    public void completeRequest(String id) {
        voMap.remove(id);
        auditionMap.remove(id);
    }

    public void setMechanism(String id,String m) {
        FullAuditInfo ai = auditionMap.get(id);
        ai.setCross_chain_mechanism(m);
    }

    public void addProcess(String id, ProcessAudit p) {
        List<ProcessAudit> l = auditionMap.get(id).getProcess();
        l.add(p);
    }

    public void addTransactionInfo(String id, TransactionAudit t) {
        //获取预设好的机制信息
        TransactionAudit transactionAudit = auditionMap.get(id).getTransaction_result();
        t.setMechanism_info(transactionAudit.getMechanism_info());
        auditionMap.get(id).setTransaction_result(t);
    }

    public String show(){
        String res = "";
        res += auditionMap.keySet();
        return res;
    }

    public void addHTLCInfo(String id, HTLCMechanismInfo h) {
        auditionMap.get(id).getTransaction_result().setMechanism_info(h);
    }

    public HTLCMechanismInfo getHTLCInfo(String id) {
        return (HTLCMechanismInfo) auditionMap.get(id).getTransaction_result().getMechanism_info();
    }

    public void addNotaryInfo(String id, NotaryMechanismInfo n) {
        auditionMap.get(id).getTransaction_result().setMechanism_info(n);
    }

    public void addDpkInfo(String id, DPKMechanismInfo d) {
        auditionMap.get(id).getTransaction_result().setMechanism_info(d);
    }

    public String getRequestIngredient(String id) throws UniException {
        if (!voMap.containsKey(id)) {
            throw new CrossChainException(500,"内部错误");
        }

        CrossChainVo request = voMap.get(id);

        return request.getGroup() + request.getDes_chain() + request.getDes_contract() + request.getDes_function() + request.getDes_args() + request.getSrc_contract() + request.getSrc_function() + request.getSrc_args() + request.getUser_name();
    }

    public String getRequestUser(String id) throws UniException {
        if (!voMap.containsKey(id)) {
            throw new CrossChainException(500,"内部错误");
        }

        CrossChainVo request = voMap.get(id);
        return request.getUser_name();
    }

    public String getTargetUser(String id) throws UniException {
        if (!voMap.containsKey(id)) {
            throw new CrossChainException(500,"内部错误");
        }

        CrossChainVo request = voMap.get(id);
        String srcArgs = request.getSrc_args();
        String[] split = srcArgs.split(",");
        return split[1];
    }

    public void uploadAuditInfo(String id) throws CrossChainException {
        FullAuditInfo entity = auditionMap.get(id);
        String payload = entity.auditInfo();
        log.info("[transaction upload data]:{}", payload);
        // 上报
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest post = ClassicRequestBuilder.post(SystemInfo.getUploadServiceAddr()).addHeader("Content-Type", "application/json").setEntity(payload.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON).build();
            client.execute(post, (r) -> {
                //receive from thgy
                log.info("received from upper platform:\n{}", r);
                return null;
            });
            log.info("upload audition info successfully");

        } catch (IOException e) {
            log.error("[upload audition info failed]:{}", e.getMessage());
        } catch (Exception e) {
            throw new CrossChainException(500, e.getMessage());
        }
    }
}