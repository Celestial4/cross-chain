package com.crosschain.common;

import com.crosschain.exception.ResolveException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CrossChainUtils {

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("sha-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("初始化哈希函数失败");
        }
    }

    public static String hash(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        byte[] digest = CrossChainUtils.digest.digest(bytes);
        for (byte b : digest) {
            sb.append(String.format("%x", b));
        }
        return sb.toString();
    }

    public static String extractInfo(String field, String source) throws UniException {
        Pattern p = Pattern.compile(String.format("(%s\"?:\\s*)(\"?)([\\w,_/().:;\\s!]+)\\2", field));
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(3);
        } else {
            throw new ResolveException(field);
        }
    }

    /**
     * status字段是数字类型的，用正则去匹配会把','匹配进去，这里做兼容性处理
     *
     * @param data
     * @return status整数
     */
    public static String extractStatusField(String data) {
        String ret = "";
        try {
            String status = CrossChainUtils.extractInfo("status", data).trim();
            if (status.contains(",")) {
                status = status.substring(0, status.indexOf(','));
            }
            ret = status;
        } catch (Exception e) {
            ret = "2";
        }
        return ret;
    }

    public static String getErrorStackInfo(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        return Arrays.toString(stackTrace);
    }

}