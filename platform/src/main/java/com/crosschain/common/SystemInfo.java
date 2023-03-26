package com.crosschain.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemInfo {

    private final static Map<String, String> maps = new HashMap<>();

    private static String selfChainName;

    public void init() {
        try {
            Properties pros = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
            pros.load(is);
            Set<Map.Entry<Object, Object>> entries = pros.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String key = (String) entry.getKey();
                if (key.equals("self")) {
                    selfChainName = (String) entry.getValue();
                    continue;
                }
                String socketAddress = (String) entry.getValue();
                maps.put(key, socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServiceAddr(String chainName) {
        return maps.get(chainName);
    }

    public static String getSelfChainName() {
        return selfChainName;
    }
}