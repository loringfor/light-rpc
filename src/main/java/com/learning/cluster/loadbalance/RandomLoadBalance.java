package com.learning.cluster.loadbalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance extends AbstractLoadBalance{
    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalance.class);

    @Override
    public String doSelect(List<String> dataList) {
        String data = null;
        int size = dataList.size();

        int totalWeight = 0;
        boolean sameWeight = true;
        int firstWeight = getWeight(dataList.get(0));
        // 下面这个循环有两个作用，第一是计算总权重 totalWeight，
        // 第二是检测每个服务提供者的权重是否相同
        for (int i = 0; i < size; i++) {
            int weight = getWeight(dataList.get(i));
            // 累加权重
            totalWeight += weight;
            // 检测当前服务提供者的权重与第一个服务提供者的权重是否相同，
            // 不相同的话，则将 sameWeight 置为 false。
            if (sameWeight && i > 0 && weight != firstWeight) {
                sameWeight = false;
            }
        }

        // 下面的 if 分支主要用于获取随机数，并计算随机数落在哪个区间上
        if (totalWeight > 0 && !sameWeight) {
            // 随机获取一个 [0, totalWeight) 区间内的数字
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            // 循环让 offset 数减去服务提供者权重值，当 offset 小于0时，返回相应的 Invoker。
            // 举例说明一下，我们有 servers = [A, B, C]，weights = [5, 3, 2]，offset = 7。
            // 第一次循环，offset - 5 = 2 > 0，即 offset > 5，
            // 表明其不会落在服务器 A 对应的区间上。
            // 第二次循环，offset - 3 = -1 < 0，即 5 < offset < 8，
            // 表明其会落在服务器 B 对应的区间上
            for (int i = 0; i < size; i++) {
                // 让随机值 offset 减去权重值
                offset -= getWeight(dataList.get(i));
                if (offset < 0) {
                    logger.info("using the random node:{}", dataList.get(i));
                    // 返回相应的 Invoker
                    return dataList.get(i);
                }
            }
        }

        data = dataList.get(ThreadLocalRandom.current().nextInt(size));
        logger.info("using the random node:{}", data);
        return data;
    }
}
