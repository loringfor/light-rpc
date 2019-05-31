package com.learning;

import com.learning.core.RpcServerLoader;
import com.learning.core.send.MessageSendJDKProxy;
import com.learning.registry.ServiceDiscovery;
import com.learning.serialize.RpcSerializeProtocol;
import com.learning.servicebean.Calculate;

/**
 * @author Loring
 * 测试单个实例
 */
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = null;
        // 127.0.0.1:2181为ZooKeepeer地址
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        if(serviceDiscovery != null){
            serverAddress = serviceDiscovery.discover();
        }
        // 单例模式，只有一个loader对象
        RpcServerLoader loader = RpcServerLoader.getInstance();
        RpcSerializeProtocol protocol =RpcSerializeProtocol.PROTOSTUFFSERIALIZE;
        // 建立发送的线程并提交到线程池
        loader.load(serverAddress,protocol);

        // JDK和CGLib动态代理都可以使用，CGLib的效率高于JDK
        MessageSendJDKProxy sendProxy = new MessageSendJDKProxy();
        // MessageSendCGlibProxy sendProxy = new MessageSendCGlibProxy();

        Calculate calculate = (Calculate) sendProxy.getProxy(Calculate.class);
        int add = calculate.add(10,15);
        System.out.println("calc add result:[" + add + "]");

        loader.unLoad();
    }
}
