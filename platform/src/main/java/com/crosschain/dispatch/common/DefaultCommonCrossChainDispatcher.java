package com.crosschain.dispatch.common;

import com.crosschain.audit.IAuditEntity;
import com.crosschain.common.*;
import com.crosschain.dispatch.CrossChainClient;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class DefaultCommonCrossChainDispatcher extends CommonCrossChainDispatcherBase{

    @Override
    CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws
            Exception {
        Chain desChain = group.getChain(req.getChainName());
        if (!Objects.nonNull(desChain) && desChain.getStatus() !=0) {
            throw new Exception("目标链不在跨链通道中或目标链当前不可用");
        }

        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.info(Loggers.LOGFORMAT, "received from blockchain:" + res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception {
        Chain srcChain = group.getChain(req.getChainName());
        String socAddress = systemInfo.getServiceAddr(req.getChainName());

        if (!Objects.nonNull(srcChain) && srcChain.getStatus() != 0) {
            throw new Exception("源链不在跨链通道中或源链当前不可用");
        }

        String[] socketInfo = socAddress.split(":");

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);

        log.info(Loggers.LOGFORMAT, "received from blockchain:" + res);
        return new CommonCrossChainResponse(res);
    }

    @Override
    String processResult(CommonCrossChainResponse rep) {
        //todo 此处实现对目标链的返回结果处理
        return "";
    }

    @Override
    void processAudit(IAuditEntity entity) {
        //todo 此处实现存证逻辑
    }
}