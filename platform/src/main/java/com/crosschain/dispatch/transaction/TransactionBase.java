package com.crosschain.dispatch.transaction;

import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.common.ResponseEntity;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.service.CrossChainRequest;

public abstract class TransactionBase implements Dispatcher {


    abstract CommonCrossChainResponse lock();

    abstract CommonCrossChainResponse unlock();

    abstract CommonCrossChainResponse rollback();

    @Override
    public ResponseEntity process(CrossChainRequest req) {


        return null;
    }

}