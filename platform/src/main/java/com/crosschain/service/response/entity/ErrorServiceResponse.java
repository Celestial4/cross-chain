package com.crosschain.service.response.entity;

import com.crosschain.exception.UniException;
import com.crosschain.service.response.UniResponse;

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