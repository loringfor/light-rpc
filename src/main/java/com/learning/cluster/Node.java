//package com.learning.cluster;
//
//import com.sun.management.OperatingSystemMXBean;
//
//import java.io.Serializable;
//import java.lang.management.ManagementFactory;
//
//public class Node implements Serializable {
//    private static final long serialVersionUID = 7593745554626593803L;
//    private static final long MBYTE = 1024*1024;
//
//    private String hostname;
//
//    public Node(String hostname) {
//        this.hostname = hostname;
//    }
//
//    public int getWeight(){
//        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        // 总共内存
//        long totalMemory = operatingSystemMXBean.getTotalPhysicalMemorySize()/MBYTE;
//        // 可使用内存
//        long freeMemory = operatingSystemMXBean.getFreePhysicalMemorySize()/MBYTE;
//        // 处理器数目
//        int processors = operatingSystemMXBean.getAvailableProcessors();
//        int freeMemoryRatio = (int)(100 * freeMemory/totalMemory);
//
//        // 权重为可用内存比例(0-100) + 处理器*5
//        int weight = freeMemoryRatio + processors * 5;
//        return weight;
//    }
//
//
//}
