package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainClient;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.CrossChainServiceResponse;
import com.crosschain.service.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class OtherDispatcher extends BaseDispatcher {

    protected String mode = "A";

    @Override
    public Response process(CrossChainRequest req) throws Exception {

        CommonChainRequest srcChainRequest = req.getSrcChainRequest();
        CommonChainRequest desChainRequest = req.getDesChainRequest();

        Group group = groupManager.getGroup(req.getGroup());
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            log.info("[group info]: {},{}", group.getGroupName(), group.getStatus() == 0 ? "active" : "unavailable");
            CrossChainServiceResponse response = new CrossChainServiceResponse();
            setLocalChain(req);

            try {
                String SPLITTER = "\r\n";
                String args = srcChainRequest.getArgs() + SPLITTER + desChainRequest.getChainName() + SPLITTER + desChainRequest.getArgs() + SPLITTER + mode;
                srcChainRequest.setArgs(args);

                String socAddress = SystemInfo.getServiceAddr(srcChainRequest.getChainName());
                String[] socketInfo = socAddress.split(":");
                byte[] data = CrossChainClient.innerCall(socketInfo, new String[]{srcChainRequest.getContract(), srcChainRequest.getFunction(), srcChainRequest.getArgs()});
                String res = new String(data, StandardCharsets.UTF_8);
                log.debug("received from blockchain:{}\n{}", srcChainRequest.getChainName(),res);

            } catch (Exception e) {
                throw new Exception("跨链资产转移失败："+e.getMessage());
            }
            return response;
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }

    }
}