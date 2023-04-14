package com.crosschain.audit;

import com.crosschain.service.RequestEntity;
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

    private RequestEntity request;

    public void setRequest(RequestEntity request) {
        this.request = request;
    }

    public String getRequestIngredient() {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getGroup()).append(request.getDesChain()).append(request.getDesContract()).append(request.getDesFunction()).append(request.getDesArgs()).append(request.getSrcContract()).append(request.getSrcFunction()).append(request.getSrcArgs()).append(request.getUserName());

        return sb.toString();
    }

    public  String getUserInfo() {
        return request.getUserName();
    }

    public void uploadAuditInfo(IAuditEntity entity) throws IOException {
        String payload = entity.auditInfo();
        // 上报
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            //todo 填写上传接口
            ClassicHttpRequest post = ClassicRequestBuilder.post("10.186.77.122:10010/transaction/createTransaction").addHeader("Content-Type", "application/json").setEntity(payload.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON).build();
            client.execute(post,(r)->{
                //receive from thgy
                System.out.println(r);
                return null;
            });

        } catch (IOException e) {
            log.error("[upload audition info failed]:{}",e.getMessage());
        }
    }
}