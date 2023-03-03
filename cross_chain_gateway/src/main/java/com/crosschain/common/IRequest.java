package com.crosschain.common;

public interface IRequest {

    String getDestChain();
    String getDestChannel();
    String getContract();
    String getContFunc();
    String getArgs();
}