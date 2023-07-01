package com.crosschain.audit;

import com.alibaba.fastjson2.JSON;
import com.crosschain.audit.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class FullAuditInfo implements IAuditEntity {
    String request_id;
    String cross_chain_mechanism;
    List<ProcessAudit> process;
    TransactionAudit transaction_result;
    HTLCMechanismInfo mechanism_info1;
    NotaryMechanismInfo mechanism_info2;
    DPKMechanismInfo mechanism_info3;

    @Override
    public String auditInfo() {
        return JSON.toJSONString(this);
    }
}