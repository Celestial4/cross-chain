package com.crosschain.dispatch.transaction;

import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.service.CrossChainRequest;

public abstract class TransactionBase implements Dispatcher {


    abstract CommonCrossChainResponse lock();

    abstract CommonCrossChainResponse unlock();

    abstract CommonCrossChainResponse rollback();

    @Override
    public String process(CrossChainRequest req) {
        return null;
    }
}