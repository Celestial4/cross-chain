package com.crosschain.audit;

import com.crosschain.common.SystemInfo;
import com.crosschain.service.request.CrossChainVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AuditManager {

    Map<String,IAuditEntity> cache = new ConcurrentHashMap<>();

    private CrossChainVo request;

    public void setRequest(CrossChainVo request) {
        this.request = request;
    }

    public String getRequestIngredient() {
        return request.getGroup() + request.getDesChain() + request.getDesContract() + request.getDesFunction() + request.getDesArgs() + request.getSrcContract() + request.getSrcFunction() + request.getSrcArgs() + request.getUserName();
    }

    public  String getUserInfo() {
        return request.getUserName();
    }

    public void uploadAuditInfo(IAuditEntity entity) throws IOException {
        String payload = entity.auditInfo();
        // 上报
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest post = ClassicRequestBuilder.post(SystemInfo.getUploadServiceAddr()).addHeader("Content-Type", "application/json").setEntity(payload.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON).build();
            client.execute(post,(r)->{
                //receive from thgy
                log.info("received from upper platform:\n{}",r);
                return null;
            });
            log.info("upload audition info:{}",payload);

        } catch (IOException e) {
            log.error("[upload audition info failed]:{}",e.getMessage());
            throw e;
        }
    }
}