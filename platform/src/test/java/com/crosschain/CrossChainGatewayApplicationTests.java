package com.crosschain;

import com.alibaba.fastjson2.JSON;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import com.crosschain.group.GroupManager;
import com.crosschain.service.ServerFace;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
class CrossChainGatewayApplicationTests {

    @Resource
    ServerFace app;

    @Test
    void contextLoads() {

    }

    @Test
    void test1() {
        Group group = new Group();
        group.setMembers(Arrays.asList(new Chain()));
        String jsonString = JSON.toJSONString(Arrays.asList(group));
        System.out.println(jsonString);
    }

}