package com.crosschain.dispatch.common;

import com.crosschain.audit.IAuditEntity;
import com.crosschain.common.Chain;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.common.Group;
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
        if (!Objects.nonNull(desChain) ) {
            log.debug("目标链不在跨链群组中");
            throw new Exception("目标链不在跨链群组");
        } else if (desChain.getStatus() !=0) {
            log.debug("目标链当前不可用");
            throw new Exception("目标链当前不可用");
        }
        log.info("[chain info]: {},{}",desChain.getChainName(),desChain.getStatus()==0?"active":"unavailable");
        String socAddress = systemInfo.getServiceAddr(req.getChainName());
        String[] socketInfo = socAddress.split(":");
        log.info("[dest chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}",req.getContract(),req.getFunction(),req.getArgs(),socketInfo);
        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);
        log.debug("received from blockchain:{}",res);

        return new CommonCrossChainResponse(res);
    }

    @Override
    CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception {
        Chain srcChain = group.getChain(req.getChainName());
        String socAddress = systemInfo.getServiceAddr(req.getChainName());

        if (!Objects.nonNull(srcChain) ) {
            log.info("目标链不在跨链群组中");
            throw new Exception("目标链不在跨链群组");
        } else if (srcChain.getStatus() !=0) {
            log.info("目标链当前不可用");
            throw new Exception("目标链当前不可用");
        }
        log.info("[chain info]: {},{}",srcChain.getChainName(),srcChain.getStatus()==0?"active":"unavailable");
        String[] socketInfo = socAddress.split(":");
        log.info("[src chain intercall info]:\n[contract]:{},[function]:{},[args]:{}\n[connection]:{}",req.getContract(),req.getFunction(),req.getArgs(),socketInfo);
        byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{req.getContract(), req.getFunction(), req.getArgs()});
        String res = new String(data, StandardCharsets.UTF_8);

        log.debug("received from blockchain:{}",res);
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