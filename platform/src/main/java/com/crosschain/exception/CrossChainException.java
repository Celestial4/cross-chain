package com.crosschain.exception;

public class CrossChainException extends UniException {
    private Integer code;

    private String message;

    public CrossChainException(int i, String msg) {
        super(msg);
        code = i;
        message = msg;
    }

    @Override
    public Integer getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return message;
    }
}