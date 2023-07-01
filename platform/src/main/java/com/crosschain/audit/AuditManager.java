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

    private Map<String, FullAuditInfo> cache = new ConcurrentHashMap<>(1);

    public void joinRequest(CrossChainVo request) throws UniException {
        String id = request.getRequest_id();
        if (!voMap.containsKey(id)) {
            voMap.put(id, request);
            FullAuditInfo ai = new FullAuditInfo();
            ai.setRequest_id(id);
            cache.put(id, ai);
        } else {
            throw new OperationException(String.format("%s跨链请求已经存在", id));
        }
    }

    public void completeRequest(String id) {
        voMap.remove(id);
        cache.remove(id);
    }

    public void setMechanism(String id,String m) {
        FullAuditInfo ai = cache.get(id);
        ai.setCross_chain_mechanism(m);
    }

    public void addProcess(String id, ProcessAudit p) {
        List<ProcessAudit> l = cache.get(id).getProcess();
        l.add(p);
    }

    public void addTransactionInfo(String id, TransactionAudit t) {
        cache.get(id).setTransaction_result(t);
    }

    public void addHTLCInfo(String id, HTLCMechanismInfo h) {
        cache.get(id).setMechanism_info1(h);
    }

    public HTLCMechanismInfo getHTLCInfo(String id) {
        return cache.get(id).getMechanism_info1();
    }

    public void addNotaryInfo(String id, NotaryMechanismInfo n) {
        cache.get(id).setMechanism_info2(n);
    }

    public void addDpkInfo(String id, DPKMechanismInfo d) {
        cache.get(id).setMechanism_info3(d);
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
        FullAuditInfo entity = cache.get(id);
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