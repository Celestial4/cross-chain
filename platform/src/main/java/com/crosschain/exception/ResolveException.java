package com.crosschain.exception;

public class ResolveException extends UniException {
    protected String errorField;

    public ResolveException(String message) {
        super(message);
        errorField = message;
    }

    @Override
    public Integer getErrorCode() {
        return 600;
    }

    @Override
    public String getErrorMsg() {
        return "合约返回值中不能解析出" + errorField + "字段，请查阅合约编写规范";
    }
}