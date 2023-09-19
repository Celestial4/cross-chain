package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessAudit {
    String process_time = "";
    ProcessLog process_log;
    String process_result = "";
    ExtensionInfo extensionInfo = new ExtensionInfo();

    public ProcessAudit(String process_result,ProcessLog log,ExtensionInfo info) {
        this.process_time = String.valueOf(System.currentTimeMillis());
        this.process_log = log;
        this.process_result = process_result;
        this.extensionInfo = info;
    }

    /**
     * 兼容不支持扩展信息的构造函数
     * @param process_result
     * @param log
     */
    public ProcessAudit(String process_result,ProcessLog log){
        this.process_time = String.valueOf(System.currentTimeMillis());
        this.process_log = log;
        this.process_result = process_result;
    }
}