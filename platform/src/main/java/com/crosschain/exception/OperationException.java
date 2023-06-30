package com.crosschain.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class OperationException extends UniException {
    String msg;

    @Override
    public Integer getErrorCode() {
        return 300;
    }

    @Override
    public String getErrorMsg() {
        return null;
    }
}