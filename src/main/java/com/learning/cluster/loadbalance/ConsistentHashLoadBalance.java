package com.learning.cluster.loadbalance;

import java.util.List;

public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private ConsistentHash consistentHash;

    @Override
    public String doSelect(List<String> dataList) {
        consistentHash = new ConsistentHash(5, dataList);
        return consistentHash.get(dataList);
    }
}
