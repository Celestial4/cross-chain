package com.crosschain.common;

import lombok.Data;

@Data
public class CommonCrossChainResponse {
    private String result;

    public CommonCrossChainResponse(String res) {
        result = res;
    }
}