package com.crosschain.service.response.entity;

import com.crosschain.exception.UniException;
import com.crosschain.service.response.UniResponse;

public class ErrorServiceResponse extends UniResponse {

    public ErrorServiceResponse(UniException e) {
        code = e.getErrorCode();
        message = e.getErrorMsg();
    }
}