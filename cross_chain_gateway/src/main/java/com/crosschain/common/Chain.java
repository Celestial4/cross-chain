package com.crosschain.common;

import lombok.Data;

@Data
public class Chain {
    private String chainId;
    private String chainName;
    private Integer status;
}