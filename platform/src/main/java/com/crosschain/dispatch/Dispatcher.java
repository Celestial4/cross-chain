package com.crosschain.dispatch;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.service.response.Response;

public interface Dispatcher {
    Response process(CrossChainRequest req) throws Exception;

    Response process(CommonChainRequest req) throws Exception;
}