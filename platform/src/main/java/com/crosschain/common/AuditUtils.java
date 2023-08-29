package com.crosschain.common;

import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.entity.Chain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuditUtils {

    public static ProcessAudit buildProcessAudit() {
        return new ProcessAudit();
    }

    public static ProcessLog buildProcessLog(Chain chain, String res, String desc) {
        String chainName = chain.getChainName();
        String chainType = chain.getChainType();
        Pattern p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w]+)\\2", "hash"));
        String tx_hash;
        Matcher m = p.matcher(res);
        if (m.find()) {
            tx_hash = m.group(3);
        } else {
            tx_hash = "";
        }
        return new ProcessLog(chainName, chainType, tx_hash, desc);
    }
}