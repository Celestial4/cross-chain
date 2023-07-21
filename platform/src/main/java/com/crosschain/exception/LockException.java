package com.crosschain.exception;

public class LockException extends UniException {
    public LockException(String message) {
        super(message);
    }

    @Override
    public Integer getErrorCode() {
        return 104;
    }

    @Override
    public String getErrorMsg() {
        return "lock failed";
    }
}
