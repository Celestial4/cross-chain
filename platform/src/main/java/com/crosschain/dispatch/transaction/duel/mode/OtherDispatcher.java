package com.crosschain.dispatch.transaction.duel.mode;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.SelfServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtherDispatcher extends BaseDispatcher {

    protected String mode;

    @Override
    public Response process(CrossChainRequest req) throws Exception {

        CommonChainRequest srcChainRequest = req.getSrcChainRequest();
        CommonChainRequest desChainRequest = req.getDesChainRequest();

        Group group = groupManager.getGroup(req.getGroup());
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            log.info("[group info]: {},{}", group.getGroupName(), group.getStatus() == 0 ? "active" : "unavailable");

            setLocalChain(req);

            try {
                String SPLITTER = "\r\n";
                String args = srcChainRequest.getArgs() + SPLITTER + desChainRequest.getChainName() + SPLITTER + desChainRequest.getArgs() + SPLITTER + mode;
                srcChainRequest.setArgs(args);

                String result = sendTransaction(srcChainRequest);

                return new SelfServiceResponse(result);
            } catch (Exception e) {
                throw new Exception("跨链资产转移失败："+e.getMessage());
            }
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }
    }
}