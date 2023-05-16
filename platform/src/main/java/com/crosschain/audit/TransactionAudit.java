package com.crosschain.audit;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

@Data
public class TransactionAudit implements IAuditEntity{
    Integer action;
    Integer status;
    String channel_id;
    String channel_name;
    String gateway_ids;

    String request_user_id;
    String request_user;
    String target_user_id;
    String target_user;

    String source_app_chain_contract;
    String source_app_chain_id;
    String source_app_chain_service;
    String target_app_chain_contract;
    String target_app_chain_id;
    String target_app_chain_service;

    String transaction_id;
    String transaction_proof;
    String transaction_receipt;
    String transaction_time;

    String dataHash;
    Integer volume;
    String behaviorContent;
    String behavioralResults;


    public TransactionAudit(Integer action, String channel_id, String channel_name, String gateway_ids, String request_user_id, String source_app_chain_contract, String source_app_chain_id, Integer status, String target_app_chain_contract, String target_app_chain_id, String transaction_id, String transaction_proof, String transaction_receipt, String transaction_time) {
        this.action = action;
        this.channel_id = channel_id;
        this.channel_name = channel_name;
        this.gateway_ids = gateway_ids;
        this.request_user_id = request_user_id;
        this.source_app_chain_contract = source_app_chain_contract;
        this.source_app_chain_id = source_app_chain_id;
        this.status = status;
        this.target_app_chain_contract = target_app_chain_contract;
        this.target_app_chain_id = target_app_chain_id;
        this.transaction_id = transaction_id;
        this.transaction_proof = transaction_proof;
        this.transaction_receipt = transaction_receipt;
        this.transaction_time = transaction_time;
    }

    public TransactionAudit(Integer action, Integer status, String channel_id, String channel_name, String gateway_ids, String request_user_id, String request_user, String target_user_id, String target_user, String source_app_chain_contract, String source_app_chain_id, String source_app_chain_service, String target_app_chain_contract, String target_app_chain_id, String target_app_chain_service, String transaction_id, String transaction_proof, String transaction_receipt, String transaction_time, String dataHash, Integer volume, String behaviorContent, String behavioralResults) {
        this.action = action;
        this.status = status;
        this.channel_id = channel_id;
        this.channel_name = channel_name;
        this.gateway_ids = gateway_ids;
        this.request_user_id = request_user_id;
        this.request_user = request_user;
        this.target_user_id = target_user_id;
        this.target_user = target_user;
        this.source_app_chain_contract = source_app_chain_contract;
        this.source_app_chain_id = source_app_chain_id;
        this.source_app_chain_service = source_app_chain_service;
        this.target_app_chain_contract = target_app_chain_contract;
        this.target_app_chain_id = target_app_chain_id;
        this.target_app_chain_service = target_app_chain_service;
        this.transaction_id = transaction_id;
        this.transaction_proof = transaction_proof;
        this.transaction_receipt = transaction_receipt;
        this.transaction_time = transaction_time;
        this.dataHash = dataHash;
        this.volume = volume;
        this.behaviorContent = behaviorContent;
        this.behavioralResults = behavioralResults;
    }

    @Override
    public String auditInfo() {
        return JSON.toJSONString(this);
    }
}