package com.crosschain.audit;

import com.alibaba.fastjson2.JSON;
import com.crosschain.audit.entity.*;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class FullAuditInfo implements IAuditEntity {
    String request_id;
    String cross_chain_mechanism;
    List<ProcessAudit> process = new LinkedList<>();
    TransactionAudit transaction_result = new TransactionAudit();
    HTLCMechanismInfo mechanism_info1 = new HTLCMechanismInfo();
    NotaryMechanismInfo mechanism_info2 = new NotaryMechanismInfo();
    DPKMechanismInfo mechanism_info3 = new DPKMechanismInfo();

    @Override
    public String auditInfo() {
        return JSON.toJSONString(this);
    }
}