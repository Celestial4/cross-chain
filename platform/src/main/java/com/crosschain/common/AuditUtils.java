package com.crosschain.common;

import com.alibaba.fastjson2.JSON;
import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.audit.entity.ProcessAudit;
import com.crosschain.audit.entity.ProcessLog;
import com.crosschain.common.entity.Chain;
import com.crosschain.exception.UniException;

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

        if (!CrossChainUtils.extractStatusField(res).equals("1")) {
            try {
                return new ProcessLog(chainName, chainType, tx_hash, desc, CrossChainUtils.extractInfo("data", res));
            } catch (UniException e) {
                //do noting
            }
        }
        return new ProcessLog(chainName, chainType, tx_hash, desc);
    }

    public static ProcessLog buildErrorProcessLog(Chain chain, String res, String desc, String error) {
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
        return new ProcessLog(chainName, chainType, tx_hash, desc, error);
    }

    /**
     * 从合约调用结果的json字符串构造上报数据
     *
     * @param json
     * @return
     */
    public static ExtensionInfo buildExtensionInfo(String json) {
        return JSON.parseObject(json, ExtensionInfo.class);
    }
}