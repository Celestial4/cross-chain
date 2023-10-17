package com.crosschain.common.entity;

import lombok.Data;

@Data
public class CommonChainRequest {

    private String requestId;
    private String chainName;
    private String contract;
    private String function;
    private String args="";

}