package com.learning.core;

import com.learning.core.send.MessageSendHandler;
import com.learning.core.send.MessageSendInitializeTask;
import com.learning.serialize.RpcSerializeProtocol;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 服务器配置加载
 * 客户端要加载服务端的上下文信息
 */
public class RpcServerLoader {

    private volatile static RpcServerLoader rpcServerLoader;
    //默认采用Java原生序列化协议方式传输RPC消息
    private RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.JDKSERIALIZE;

    private static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1);

    //方法返回到Java虚拟机的可用的处理器数量
    private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;

    //netty nio线程池
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);
    private MessageSendHandler messageSendHandler=null;

    //等待Netty服务端链路建立通知信号
    private Lock lock = new ReentrantLock();
    private Condition signal = lock.newCondition();

    private RpcServerLoader(){}

    // 并发双重锁定实现单例模式
    public static RpcServerLoader getInstance(){
        if (rpcServerLoader==null){
            synchronized (RpcServerLoader.class){
                if(rpcServerLoader==null){
                    rpcServerLoader = new RpcServerLoader();
                }
            }
        }
        return rpcServerLoader;
    }

    public void load(String serverAddress, RpcSerializeProtocol protocol){
        String[] ipAddr = serverAddress.split(":");
        if(ipAddr.length==2){
            String host = ipAddr[0];
            int port = Integer.parseInt(ipAddr[1]);
            InetSocketAddress remoteAddr = new InetSocketAddress(host,port);
            threadPoolExecutor.submit(new MessageSendInitializeTask(eventLoopGroup, remoteAddr, this, protocol));
        }
    }

    public void unLoad(){
        messageSendHandler.close();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

    public MessageSendHandler getMessageSendHandler() throws InterruptedException {
        try {
            lock.lock();
            //Netty服务端链路没有建立完毕之前，先挂起等待
            if (messageSendHandler == null) {
                signal.await();
            }
            return messageSendHandler;
        } finally {
            lock.unlock();
        }
    }

    public void setMessageSendHandler(MessageSendHandler messageSendHandler) {
        try {
            lock.lock();
            this.messageSendHandler = messageSendHandler;
            //唤醒所有等待客户端RPC线程
            signal.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
