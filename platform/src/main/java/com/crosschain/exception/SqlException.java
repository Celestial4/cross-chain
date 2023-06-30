package com.crosschain.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SqlException extends UniException {
    private String msg;

    @Override
    public Integer getErrorCode() {
        return 400;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}