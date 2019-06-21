package com.learning.cluster.loadbalance;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private ConsistentHash consistentHash;

    @Override
    public String doSelect(List<String> dataList) {
        consistentHash = new ConsistentHash(5, dataList);
        return consistentHash.get(dataList);
    }

    // 一致性Hash的具体实现，基于TreeMap
    private static class ConsistentHash {
        private final ResidualHashFunction hashFunction = new ResidualHashFunction();
        private final int numberOfReplicas;
        private final SortedMap<Integer,String> circle = new TreeMap<>();

        public ConsistentHash(int numberOfReplicas, List<String> nodeList) {
            this.numberOfReplicas = numberOfReplicas;
            for (String node : nodeList) {
                add(node);
            }
        }

        public void add(String node) {
            for (int i = 0; i <numberOfReplicas; i++) {
                circle.put(hashFunction.hash(node + i), node);
            }
        }

        public void remove(String node) {
            for (int i = 0; i <numberOfReplicas; i++) {
                circle.remove(hashFunction.hash(node + i));
            }
        }

        public String get(Object key) {
            if (circle.isEmpty()) {
                return null;
            }
            int hash = hashFunction.hash(key);
            //
            if (!circle.containsKey(hash)) {
                SortedMap<Integer, String> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }

        public static class ResidualHashFunction<T>{
            public int hash(T t) {
                return t.hashCode() % 100;
            }
        }
    }
}
