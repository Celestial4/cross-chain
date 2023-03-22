package com.crosschain.dispatch.transaction;

import com.crosschain.common.CommonCrossChainResponse;

public class HTLCTransactionDispatcher extends TransactionBase{
    @Override
    CommonCrossChainResponse lock() {
        return null;
    }

    @Override
    CommonCrossChainResponse unlock() {
        return null;
    }

    @Override
    CommonCrossChainResponse rollback() {
        return null;
    }
}