package com.crosschain.dispatch.self;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.SelfServiceResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
@Slf4j
public class SelfDispatcher extends BaseDispatcher {
    @Override
    public Response process(CommonChainRequest req) throws Exception {
        String socAddress = SystemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[inter_call info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});

        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}\n{}", req.getChainName(),res);

        return new SelfServiceResponse(res);
    }
}