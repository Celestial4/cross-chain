package com.crosschain.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ResolveException extends UniException{
    protected String errorField;

    @Override
    public Integer getErrorCode() {
        return 600;
    }

    @Override
    public String getErrorMsg() {
        return "合约返回值中不能解析出" + errorField + "字段，请查阅合约编写规范";
    }
}