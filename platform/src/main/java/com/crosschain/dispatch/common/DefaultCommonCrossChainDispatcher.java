package com.crosschain.dispatch.common;

import com.crosschain.common.*;
import com.crosschain.dispatch.CrossChainClient;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class DefaultCommonCrossChainDispatcher extends CommonCrossChainDispatcherBase{

    @Override
    CommonCrossChainResponse processDes(CommonCrossChainRequest req, Channel channel) throws
            Exception {
        Chain desChain = channel.getChain(req.getChainName());
        if (!Objects.nonNull(desChain) && desChain.getStatus() !=0) {
            throw new Exception("目标链不在跨链通道中或目标链当前不可用");
        }

        String socAddress = maps.get(req.getChainName());
        String[] socketInfo = socAddress.split(":");

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.info(Loggers.LOGFORMAT, "received from blockchain:" + res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    void processSrc(CommonCrossChainRequest req, Channel channel) throws Exception {
        Chain srcChain = channel.getChain(req.getChainName());
        String socAddress = maps.get(req.getChainName());

        if (!Objects.nonNull(srcChain) && srcChain.getStatus() != 0) {
            throw new Exception("源链不在跨链通道中或源链当前不可用");
        }

        String[] socketInfo = socAddress.split(":");

        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);

        log.info(Loggers.LOGFORMAT, "received from blockchain:" + res);
    }

    //todo 对目标链的返回结果处理
    @Override
    String processResult(CommonCrossChainResponse rep) {
        return null;
    }
}