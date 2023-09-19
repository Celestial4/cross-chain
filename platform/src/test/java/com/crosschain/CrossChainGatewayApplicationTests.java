package com.crosschain;

import com.crosschain.audit.entity.ExtensionInfo;
import com.crosschain.common.AuditUtils;
import com.crosschain.service.ServerFace;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CrossChainGatewayApplicationTests {

    @Resource
    ServerFace app;

    @Test
    void contextLoads() {

    }

    @Test
    void test1() {
        String s = "{\n" +
                "\t\"ret\": \"0x6a61636b2c2062616c616e63653d35303031382e302c206c6f636b3d302e30\",\n" +
                "\t\"vmType\": \"HVM\",\n" +
                "\t\"gasUsed\": 672596,\n" +
                "\t\"code\": 200,\n" +
                "\t\"Log\": [],\n" +
                "\t\"retMessage\": \"jack, balance=50018.0, lock=0.0\",\n" +
                "\t\"contractAddress\": \"0x30632d8fe786b9d7ec27f988126564ea3986cca3\",\n" +
                "\t\"state\": 1,\n" +
                "\t\"message\": \"success\",\n" +
                "\t\"jsonrpc\": \"2.0\",\n" +
                "\t\"txHash\": \"0x178639d6af25f93a7e2cd544139f64cccd939425f30772ac1ebf0800832ce109\",\n" +
                "\t\"version\": \"3.6\",\n" +
                "\t\"test\":123\n" +
                "}";
        ExtensionInfo extensionInfo = AuditUtils.buildExtensionInfo(s);
    }

}