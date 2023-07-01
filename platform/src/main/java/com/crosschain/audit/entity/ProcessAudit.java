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
}