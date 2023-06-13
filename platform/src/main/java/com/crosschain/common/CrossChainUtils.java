package com.crosschain.common;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}