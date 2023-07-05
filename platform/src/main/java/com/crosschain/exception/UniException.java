package com.crosschain.exception;

public abstract class UniException extends Exception {
    public UniException(String message) {
        super(message);
    }

    public abstract Integer getErrorCode();

    public abstract String getErrorMsg();
}