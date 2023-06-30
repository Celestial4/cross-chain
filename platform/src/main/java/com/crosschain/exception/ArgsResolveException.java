package com.crosschain.exception;

public class ArgsResolveException extends ResolveException{

    public ArgsResolveException(String errorField) {
        super(errorField);
    }

    @Override
    public String getErrorMsg() {
        return "请求参数中不能解析出" + errorField + "字段，请查阅合约编写规范";
    }
}