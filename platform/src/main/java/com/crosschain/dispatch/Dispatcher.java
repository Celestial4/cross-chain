package com.crosschain.dispatch;

import com.crosschain.common.ResponseEntity;
import com.crosschain.service.CrossChainRequest;

public interface Dispatcher {
    ResponseEntity process(CrossChainRequest req);
}