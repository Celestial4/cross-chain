package com.crosschain.dispatch.self;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.SelfServiceResponse;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SelfDispatcher extends BaseDispatcher {
    @Override
    public Response process(CommonChainRequest req) throws Exception {

        log.info("[---self call info---]\n");
        String res = sendTransaction(req);

        return new SelfServiceResponse(res);
    }
}