package com.learning.registry;

import com.learning.common.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private CountDownLatch latch = new CountDownLatch(1);
    private String registryAddress;
    private volatile List<String> dataList = new ArrayList<>();

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        ZooKeeper zooKeeper = connectServer();
        if(zooKeeper!=null){
            watchNode(zooKeeper);
        }
    }

    public String discover(){
        String data = null;
        int size = dataList.size();
        if(size>0){
            if(size==1){
                data = dataList.get(0);
                logger.info("using the only node:{}", data);

            }else{
                // 随机选取一个节点
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                logger.info("using the random node:{}", data);
            }
        }
        return data;
    }
    public ZooKeeper connectServer(){
        ZooKeeper zooKeeper=null;
        try{
            zooKeeper = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if(event.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        }catch (IOException | InterruptedException e){
            logger.error("erro in connect ZooKeeper server,{}", e);
        }
        return zooKeeper;
    }

    private void watchNode(ZooKeeper zooKeeper){
        try {
            List<String> nodeList = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        watchNode(zooKeeper);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zooKeeper.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            this.dataList = dataList;
        }catch (KeeperException|InterruptedException e){
            logger.error("erro:{}", e);
        }
    }


}