package com.crosschain.dispatch.basic.mode;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.CommonChainResponse;
import com.crosschain.common.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.basic.InfoSharingDispatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultInfoSharingDispatcher extends InfoSharingDispatcher {

    @Override
    protected CommonChainResponse processDes(CommonChainRequest req, Group group) throws
            Exception {
        checkAvailable(group, req);
        log.info("[---dest call info---]\n");
        String res = sendTransaction(req);

        return new CommonChainResponse(res);
    }

    @Override
    protected CommonChainResponse processSrc(CommonChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);
        log.info("[---src call info---]\n");
        String res = sendTransaction(req);

        return new CommonChainResponse(res);
    }

    @Override
    protected String processResult(CommonChainResponse rep) {
        return null;
    }

    @Override
    protected void processAudit(CrossChainRequest req, String msgRtd) throws Exception {

    }
}