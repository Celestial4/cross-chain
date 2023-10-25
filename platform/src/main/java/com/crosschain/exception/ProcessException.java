package com.crosschain.exception;

public class ProcessException extends UniException{
    private String msg;

    public ProcessException(String message) {
        super(message);
        msg = message;
    }

    @Override
    public Integer getErrorCode() {
        return 800;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}