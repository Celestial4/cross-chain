package com.crosschain.service.response;

public class ErrorServiceResponse implements Response {

    String errorMsg;

    public ErrorServiceResponse(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String get() {
        return errorMsg;
    }
}