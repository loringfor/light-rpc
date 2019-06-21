package com.learning.services;

import com.learning.core.RpcServerLoader;
import com.learning.core.send.MessageSendJDKProxy;
import com.learning.registry.ServiceDiscovery;
import com.learning.serialize.RpcSerializeProtocol;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class RpcParallelTest {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = null;
        // 127.0.0.1:2181为ZooKeepeer地址
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("192.168.1.127:2181");
        if(serviceDiscovery != null){
            serverAddress = serviceDiscovery.discover();
        }
        logger.info("serverAddress is {}",serverAddress);
        // 单例模式，只有一个loader对象
        RpcServerLoader loader = RpcServerLoader.getInstance();
        RpcSerializeProtocol protocol =RpcSerializeProtocol.PROTOSTUFFSERIALIZE;
        // 建立发送的线程并提交到线程池
        loader.load(serverAddress,protocol);

        // JDK和CGLib动态代理都可以使用，CGLib的效率高于JDK
        MessageSendJDKProxy sendProxy = new MessageSendJDKProxy();
//        MessageSendCGlibProxy sendProxy = new MessageSendCGlibProxy();
        //并行度
        int parallel=10;

        //开始计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CountDownLatch signal = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(parallel);

        for(int i=0;i<parallel;i++){
            ParallelRequestThread parallelRequestThread =new ParallelRequestThread(sendProxy, signal, finish, i);
            new Thread(parallelRequestThread).start();
        }

        signal.countDown();
        finish.await();
        stopWatch.stop();

        String tip = String.format("RPC调用总共耗时: [%s] 毫秒", stopWatch.getTime());
        System.out.println(tip);

        loader.unLoad();
    }
}
