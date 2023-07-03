package com.crosschain.statistics;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class STATCSManager {
    static SystemInfo si = new SystemInfo();
    static HardwareAbstractionLayer hal = si.getHardware();
    static OperatingSystem os = si.getOperatingSystem();

    public static String getCpuInfo() {
        getOs();
        getComputerInfo();
        StatisticsHelper.printCpu(hal.getProcessor());
        //StatisticsHelper.printProcessor(hal.getProcessor());
        return StatisticsHelper.printInfo();
    }

    public static String getMemoryInfo() {
        getOs();
        getComputerInfo();
        StatisticsHelper.printMemory(hal.getMemory());
        return StatisticsHelper.printInfo();
    }

    public static void getOs() {
        StatisticsHelper.printOperatingSystem(os);
    }

    public static void getComputerInfo() {
        StatisticsHelper.printComputerSystem(hal.getComputerSystem());
    }
}