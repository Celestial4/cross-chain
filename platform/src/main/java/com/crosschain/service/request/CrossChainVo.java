package com.crosschain.service.request;

import lombok.Data;

@Data
public class CrossChainVo {
    String group;
    String desChain;
    String desContract;
    String desFunction;
    String desArgs = "";

    String srcContract;
    String srcFunction;
    String srcArgs = "";
    String mode = "default";
    String userName;
    String userToken;
}