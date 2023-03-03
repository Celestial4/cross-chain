package com.crosschain.common;

public class CCRequest implements IRequest {
    private String sender;
    private String desChannel;
    private String desChain;
    private String contract;
    private String contcFunc;
    private String args;

    public void setDesChannel(String desChannel) {
        this.desChannel = desChannel;
    }

    public void setDesChain(String desChain) {
        this.desChain = desChain;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public void setContcFunc(String contcFunc) {
        this.contcFunc = contcFunc;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public String getDestChain() {
        return desChain;
    }

    @Override
    public String getDestChannel() {
        return desChannel;
    }

    @Override
    public String getContract() {
        return contract;
    }

    @Override
    public String getContFunc() {
        return contcFunc;
    }

    @Override
    public String getArgs() {
        return args;
    }
}