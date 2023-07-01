package com.crosschain.service.request;

import lombok.Data;

@Data
public class CrossChainVo {
    String request_id;
    String group;
    String des_chain;
    String des_contract;
    String des_function;
    String des_args = "";

    String src_contract;
    String src_function;
    String src_args = "";
    String mode = "default";
    String user_name;
    String user_token;
}