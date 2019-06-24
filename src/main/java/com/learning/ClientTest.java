package com.learning;

import com.learning.cluster.FailOverClusterInvoker;
import com.learning.core.send.MessageSendCGlibProxy;
import com.learning.core.send.MessageSendJDKProxy;
import com.learning.registry.ServiceDiscovery;
import com.learning.serialize.RpcSerializeProtocol;
/**
 * @author Loring
 * 测试单个实例
 */
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        // 127.0.0.1:2181为ZooKeepeer地址
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("192.168.1.127:2181");
        if(serviceDiscovery == null){
            System.out.println("连接ZooKeeper注册中心失败");
            return;
        }
        int parallel = Runtime.getRuntime().availableProcessors() * 2;
        RpcSerializeProtocol protocol =RpcSerializeProtocol.PROTOSTUFFSERIALIZE;
        FailOverClusterInvoker invoker = new FailOverClusterInvoker(serviceDiscovery, parallel, protocol, 5);
        invoker.invoke();
    }
}
