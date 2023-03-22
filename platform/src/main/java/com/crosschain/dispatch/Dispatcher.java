package com.crosschain.dispatch;

import com.crosschain.service.CrossChainRequest;

public interface Dispatcher {
    String process(CrossChainRequest req);
}