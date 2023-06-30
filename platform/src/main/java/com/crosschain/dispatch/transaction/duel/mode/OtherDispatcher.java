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
        checkAvailable(group,srcChainRequest);

        setLocalChain(req);

        String SPLITTER = ",";
        String args = srcChainRequest.getArgs() + SPLITTER + desChainRequest.getChainName() + SPLITTER + desChainRequest.getArgs() + SPLITTER + mode;
        srcChainRequest.setArgs(args);

        String result = sendTransaction(srcChainRequest);

        return new SelfServiceResponse(result);
    }
}