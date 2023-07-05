package com.crosschain.exception;

public class OperationException extends UniException {
    String msg;

    public OperationException(String message) {
        super(message);
        msg = message;
    }

    @Override
    public Integer getErrorCode() {
        return 300;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}