package com.crosschain.dispatch;

import com.crosschain.service.ResponseEntity;

public interface Dispatcher {
    ResponseEntity process(CrossChainRequest req) throws Exception;
}