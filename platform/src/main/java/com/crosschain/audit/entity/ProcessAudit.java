package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessAudit {
    String process_time;
    String process_log;
    String process_result;


    public ProcessAudit(String process_log, String process_result) {
        this.process_time = String.valueOf(System.currentTimeMillis());
        this.process_log = process_log;
        this.process_result = process_result;
    }
}