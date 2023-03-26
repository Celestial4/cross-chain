package com.crosschain.service;

import lombok.Data;

@Data
public class RequestEntity {
    String group;
    String desChain;
    String desContract;
    String desFunction;
    String args="";

    String srcContract;
    String srcFunction;
    String mode="default";
    String userName;
    String userToken;
}