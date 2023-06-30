package com.crosschain.service.response;

import com.crosschain.exception.UniException;

public class ErrorServiceResponse extends UniResponse {

    UniException e;

    public ErrorServiceResponse(UniException exception) {
        this.e = exception;
    }

    @Override
    public String get() {
        code = e.getErrorCode();
        message = e.getErrorMsg();
        return super.get();
    }
}