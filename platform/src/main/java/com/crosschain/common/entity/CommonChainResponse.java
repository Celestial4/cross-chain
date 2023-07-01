package com.crosschain.common.entity;

import lombok.Data;

@Data
public class CommonChainResponse {
    private String result;

    public CommonChainResponse(String res) {
        result = res;
    }
}