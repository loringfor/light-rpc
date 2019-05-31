package com.learning.registry;

import com.learning.common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private String registryAddress;
    private CountDownLatch latch = new CountDownLatch(1);

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data){
        if(data!=null){
            ZooKeeper zooKeeper=connectServer();
            if(zooKeeper!=null){
                createNode(zooKeeper,data);
            }
        }
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
            logger.error("error in connect ZooKeeper server");
        }
        return zooKeeper;
    }

    public void createNode(ZooKeeper zooKeeper,String data){
        try {
            byte[] bytes = data.getBytes();
            String path = zooKeeper.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("create zookeeper node,path:{},data:{}", path, data);
        } catch (KeeperException | InterruptedException e) {
            logger.error("{}",e);
        }
    }

}
