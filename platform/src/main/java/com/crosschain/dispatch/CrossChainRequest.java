package com.crosschain.dispatch;

import com.crosschain.common.CommonChainRequest;

public class CrossChainRequest{
    private CommonChainRequest srcChain;
    private CommonChainRequest desChain;
    private String group;


    public CrossChainRequest(CommonChainRequest srcChain, CommonChainRequest desChain, String group) {
        this.srcChain = srcChain;
        this.desChain = desChain;
        this.group = group;
    }

    public CommonChainRequest getSrcChainRequest() {
        return srcChain;
    }

    public CommonChainRequest getDesChainRequest() {
        return desChain;
    }

    public String getGroup() {
        return group;
    }
}