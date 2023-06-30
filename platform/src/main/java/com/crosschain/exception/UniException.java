package com.crosschain.exception;

public abstract class UniException extends Exception {

    public abstract Integer getErrorCode();

    public abstract String getErrorMsg();
}