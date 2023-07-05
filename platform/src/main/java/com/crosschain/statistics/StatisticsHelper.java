package com.crosschain.statistics;

import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.PhysicalProcessor;
import oshi.hardware.CentralProcessor.ProcessorCache;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OSSession;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsHelper {
    private static List<String> oshi = new ArrayList<>();

    public static String printInfo() {
        StringBuilder output = new StringBuilder();
        for (String line : oshi) {
            output.append(line);
            if (line != null && !line.endsWith("\n")) {
                output.append('\n');
            }
        }
        oshi.clear();
        return output.toString();
    }

    public static void printOperatingSystem(final OperatingSystem os) {
        oshi.add(String.valueOf(os));
        oshi.add("Booted: " + Instant.ofEpochSecond(os.getSystemBootTime()));
        oshi.add("Uptime: " + FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        oshi.add("Running with" + (os.isElevated() ? "" : "out") + " elevated permissions.");
        oshi.add("Sessions:");
        for (OSSession s : os.getSessions()) {
            oshi.add(" " + s.toString());
        }
    }

    public static void printComputerSystem(final ComputerSystem computerSystem) {
        oshi.add("System: " + computerSystem.toString());
        oshi.add(" Firmware: " + computerSystem.getFirmware().toString());
        oshi.add(" Baseboard: " + computerSystem.getBaseboard().toString());
    }

    public static void printProcessor(CentralProcessor processor) {
        oshi.add(processor.toString());

        Map<Integer, Integer> efficiencyCount = new HashMap<>();
        int maxEfficiency = 0;
        for (PhysicalProcessor cpu : processor.getPhysicalProcessors()) {
            int eff = cpu.getEfficiency();
            efficiencyCount.merge(eff, 1, Integer::sum);
            if (eff > maxEfficiency) {
                maxEfficiency = eff;
            }
        }
        oshi.add(" Topology:");
        oshi.add(String.format("  %7s %4s %4s %4s %4s %4s", "LogProc", "P/E", "Proc", "Pkg", "NUMA", "PGrp"));
        for (PhysicalProcessor cpu : processor.getPhysicalProcessors()) {
            oshi.add(String.format("  %7s %4s %4d %4s %4d %4d",
                    processor.getLogicalProcessors().stream()
                            .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                            .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                            .map(p -> Integer.toString(p.getProcessorNumber())).collect(Collectors.joining(",")),
                    cpu.getEfficiency() == maxEfficiency ? "P" : "E", cpu.getPhysicalProcessorNumber(),
                    cpu.getPhysicalPackageNumber(),
                    processor.getLogicalProcessors().stream()
                            .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                            .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                            .mapToInt(p -> p.getNumaNode()).findFirst().orElse(0),
                    processor.getLogicalProcessors().stream()
                            .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                            .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                            .mapToInt(p -> p.getProcessorGroup()).findFirst().orElse(0)));
        }
        List<ProcessorCache> caches = processor.getProcessorCaches();
        if (!caches.isEmpty()) {
            oshi.add(" Caches:");
        }
        for (int i = 0; i < caches.size(); i++) {
            ProcessorCache cache = caches.get(i);
            boolean perCore = cache.getLevel() < 3;
            boolean pCore = perCore && i < caches.size() - 1 && cache.getLevel() == caches.get(i + 1).getLevel()
                    && cache.getType() == caches.get(i + 1).getType();
            boolean eCore = perCore && i > 0 && cache.getLevel() == caches.get(i - 1).getLevel()
                    && cache.getType() == caches.get(i - 1).getType();
            StringBuilder sb = new StringBuilder("  ").append(cache);
            if (perCore) {
                sb.append(" (per ");
                if (pCore) {
                    sb.append("P-");
                } else if (eCore) {
                    sb.append("E-");
                }
                sb.append("core)");
            }
            oshi.add(sb.toString());
        }
    }

    public static void printMemory(GlobalMemory memory) {
        long memoryTotal = memory.getTotal();
        long available = memory.getAvailable();
        Double total_t = Double.valueOf(Long.toString(memoryTotal));
        Double total_a = Double.valueOf(Long.toString(available));
        double usage_d = (total_t-total_a)/total_t;
        oshi.add(String.format("{\"total\":%d,\"used\":%d,\"usage\":%.2f%%}", memoryTotal, memoryTotal - available, usage_d));
    }

    public static void printCpu(CentralProcessor processor) {
        double usage = processor.getSystemCpuLoad(1000);
        int cnt = processor.getPhysicalProcessorCount();
        oshi.add(String.format("{\"total\":%d,\"used\":%d,\"usage\":%.2f%%}", cnt, cnt, usage * 100));
    }

    public static void printProcesses(OperatingSystem os, GlobalMemory memory) {
        OSProcess myProc = os.getProcess(os.getProcessId());
        // current process will never be null. Other code should check for null here
        oshi.add(
                "My PID: " + myProc.getProcessID() + " with affinity " + Long.toBinaryString(myProc.getAffinityMask()));
        oshi.add("My TID: " + os.getThreadId() + " with details " + os.getCurrentThread());

        oshi.add("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        // Sort by highest CPU
        List<OSProcess> procs = os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES, OperatingSystem.ProcessSorting.CPU_DESC, 5);
        oshi.add("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size(); i++) {
            OSProcess p = procs.get(i);
            oshi.add(String.format(" %5d %5.1f %4.1f %9s %9s %s", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                    FormatUtil.formatBytes(p.getResidentSetSize()), p.getName()));
        }
        OSProcess p = os.getProcess(os.getProcessId());
        oshi.add("Current process arguments: ");
        for (String s : p.getArguments()) {
            oshi.add("  " + s);
        }
        oshi.add("Current process environment: ");
        for (Map.Entry<String, String> e : p.getEnvironmentVariables().entrySet()) {
            oshi.add("  " + e.getKey() + "=" + e.getValue());
        }
    }
}