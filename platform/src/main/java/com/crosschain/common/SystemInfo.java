package com.crosschain.common;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemInfo {

    private final static Map<String, String> maps = new HashMap<>();

    private static String selfChainName;

    private static String uploadAddr;

    public void init() {
        readConnectionInfo();
        readUpload();
    }

    public static String getServiceAddr(String chainName) {
        return maps.get(chainName);
    }

    public static String getSelfChainName() {
        return selfChainName;
    }

    public static String getUploadServiceAddr() {
        return uploadAddr;
    }

    private void readUpload() {
        try {
            Properties pros = new Properties();
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            InputStream is = pathMatchingResourcePatternResolver.getResource("file:conf/thgy.properties").getInputStream();
            pros.load(is);
            uploadAddr = pros.getProperty("ip");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readConnectionInfo() {
        try {
            Properties pros = new Properties();
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            InputStream is = pathMatchingResourcePatternResolver.getResource("file:conf/config.properties").getInputStream();
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
}