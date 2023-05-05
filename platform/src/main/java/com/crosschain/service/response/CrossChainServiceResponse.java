package com.crosschain.service.response;

import lombok.Data;

@Data
public class CrossChainServiceResponse implements Response {

    String desResult;
    String srcResult;

    @Override
    public String get() {
        return String.format("[desChainResult]:---\n%s\n[srcChainResult]:---\n%s\n", desResult, srcResult);
    }
}