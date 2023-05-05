package com.crosschain.service.request;

import lombok.Data;

@Data
public class SelfVo {

    String mode = "self";

    String contract;
    String function;
    String args;
}