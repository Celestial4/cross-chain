package com.crosschain.exception;

public class SqlException extends UniException {
    private String msg;

    public SqlException(String message) {
        super(message);
        msg = message;
    }

    @Override
    public Integer getErrorCode() {
        return 400;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}