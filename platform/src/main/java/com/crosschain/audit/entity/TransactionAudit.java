package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TransactionAudit {
    String action = "";
    Integer status = 2;
    String channel_name = "";
    String gateway_ids = "";

    String request_user_id = "";
    String request_user = "";
    String target_user_id = "";
    String target_user = "";

    String source_app_chain_contract = "";
    String source_app_chain_id = "";
    String source_app_chain_service = "";
    String source_app_chain_type = "";
    String target_app_chain_contract = "";
    String target_app_chain_id = "";
    //链名
    String target_app_chain_service = "";
    //链类型
    String target_app_chain_type = "";

    String transaction_id = "";
    String transaction_proof = "";
    String transaction_receipt = "";
    String transaction_time = "";

    String data_hash = "";
    Integer volume;
    String behavior_content = "";
    String behavioral_results = "";
    List<Mechanism> mechanism_info = new ArrayList<>();
}