package com.crosschain.common;

import com.crosschain.exception.CrossChainException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.*;

public class SystemInfo {

    private final static Map<String, String> maps = new HashMap<>();

    private static String selfChainName;

    private static String uploadAddr;

    public void init() {
        readConnectionInfo();
        readUpload();
    }

    public static String getServiceAddr(String chainName) throws Exception {
        String serviceAddr = maps.get(chainName);
        if (Objects.isNull(serviceAddr)) {
            throw new CrossChainException(501,String.format("找不到跨链服务组件:%s",chainName));
        }
        return serviceAddr;
    }

    public static String getGatewayAddr(String chainName) throws CrossChainException {
        String gatewayAddr = maps.get("gateway-"+ chainName);
        if (Objects.isNull(gatewayAddr)) {
            throw new CrossChainException(501,String.format("找不到跨链网关信息:%s",chainName));
        }
        return gatewayAddr;
    }

    public static String getSelfChainName() throws Exception{
        if (Strings.isEmpty(selfChainName)) {
            throw new CrossChainException(503, "请检查conf/config.properties配置文件");
        }

        return selfChainName;
    }

    public static String getUploadServiceAddr() throws Exception{
        if (Strings.isEmpty(uploadAddr)) {
            throw new CrossChainException(502, "没有找到事务上传接口，请检查conf/thgy.properties配置文件");
        }
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