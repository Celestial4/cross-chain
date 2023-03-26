package com.crosschain.common;

import lombok.Data;

@Data
public class CommonCrossChainRequest {

    private String chainName;
    private String contract;
    private String function;
    private String args="";

}