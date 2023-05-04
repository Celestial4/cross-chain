package com.crosschain.dispatch.basic.mode;

import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.common.Group;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.basic.InfoSharingDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class DefaultInfoSharingDispatcher extends InfoSharingDispatcher {

    @Override
    protected CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws
            Exception {
        checkAvailable(group, req);

        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[dest chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    protected CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);

        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}", req.getContract(), req.getFunction(), req.getArgs(), socketInfo);

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}", res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    protected String processResult(CommonCrossChainResponse rep) {
        return null;
    }

    @Override
    protected void processAudit(CrossChainRequest req, String msgRtd) throws Exception {

    }
}