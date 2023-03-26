package com.crosschain.service;

import com.crosschain.common.CommonCrossChainRequest;

public class CrossChainRequest{
    private CommonCrossChainRequest srcChain;
    private CommonCrossChainRequest desChain;
    private String group;


    public CrossChainRequest(CommonCrossChainRequest srcChain, CommonCrossChainRequest desChain, String group) {
        this.srcChain = srcChain;
        this.desChain = desChain;
        this.group = group;
    }

    public CommonCrossChainRequest getSrcChainRequest() {
        return srcChain;
    }

    public CommonCrossChainRequest getDesChainRequest() {
        return desChain;
    }

    public String getGroup() {
        return group;
    }
}