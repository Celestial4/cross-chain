package com.crosschain.common.entity;

import lombok.Data;

@Data
public class Chain {
    private String chainId;
    private String chainName;
    private Integer status;
    private String chainType;
}