package com.crosschain.common;

public class CrossChainRequest{
    private CommonCrossChainRequest srcChain;
    private CommonCrossChainRequest desChain;
    private String channel;


    public CrossChainRequest(CommonCrossChainRequest srcChain, CommonCrossChainRequest desChain, String channel) {
        this.srcChain = srcChain;
        this.desChain = desChain;
        this.channel = channel;
    }

    public CommonCrossChainRequest getSrcChainRequest() {
        return srcChain;
    }

    public CommonCrossChainRequest getDesChainRequest() {
        return desChain;
    }

    public String getChannel() {
        return channel;
    }
}