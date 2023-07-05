package com.example;

import com.crosschain.statistics.STATCSManager;
import org.junit.jupiter.api.Test;

public class TestStatistic {

    @Test
    void test1() {
        String cpuInfo = STATCSManager.getCpuInfo();
        System.out.println(cpuInfo);
    }
}