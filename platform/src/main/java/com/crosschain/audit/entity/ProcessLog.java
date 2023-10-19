package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessLog {
    String chain_name;
    String chain_type;
    String tx_hash;
    String desc;
    String log_type = "";
    String error = "";

    public ProcessLog(String chainName, String chainType, String tx_hash, String desc) {
        chain_name = chainName;
        chain_type = chainType;
        this.tx_hash = tx_hash;
        this.desc = desc;
    }

    public ProcessLog(String chainName, String chainType, String tx_hash, String desc, String error) {
        chain_name = chainName;
        chain_type = chainType;
        this.tx_hash = tx_hash;
        this.desc = desc;
        this.error = error;
    }
}