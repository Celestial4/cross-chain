package com.crosschain.common;

import lombok.Data;

@Data
public class CommonChainResponse {
    private String result;

    public CommonChainResponse(String res) {
        result = res;
    }
}