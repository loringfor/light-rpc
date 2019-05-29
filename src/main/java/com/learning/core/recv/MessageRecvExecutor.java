package com.learning.core.recv;

import com.learning.core.NameThreadFactory;
import com.learning.core.RpcThreadPool;
import com.learning.model.MessageKeyVal;
import com.learning.serialize.RpcSerializeProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageRecvExecutor implements ApplicationContextAware, InitializingBean {
    private final String DELIMITER = ":";

    private String serverAddress;
    //默认JKD本地序列化协议
    private RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.JDKSERIALIZE;
    private Map<String, Object> concurrentHashMap = new ConcurrentHashMap<String, Object>();
    private static ThreadPoolExecutor threadPoolExecutor;

    private static MessageRecvExecutor messageRecvExecutor;
    public static MessageRecvExecutor getInstance(){
        if (messageRecvExecutor==null){
            synchronized (MessageRecvExecutor.class){
                messageRecvExecutor = new MessageRecvExecutor();
            }
        }
        return messageRecvExecutor;
    }

    public MessageRecvExecutor(){}
    public MessageRecvExecutor(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    public MessageRecvExecutor(String serverAddress, RpcSerializeProtocol serializeProtocol) {
        this.serverAddress = serverAddress;
        this.serializeProtocol = serializeProtocol;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public static void submit(Runnable task){
        if(threadPoolExecutor==null){
            synchronized (MessageRecvExecutor.class){
                if(threadPoolExecutor==null){
                    threadPoolExecutor = (ThreadPoolExecutor) RpcThreadPool.getExecutor(16,-1);
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            MessageKeyVal messageKeyVal = (MessageKeyVal) applicationContext.getBean(Class.forName("com.learning.model.MessageKeyVal"));
            Map<String,Object> map = messageKeyVal.getMessageKeyVal();
            Iterator iterator =map.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                concurrentHashMap.put(entry.getKey().toString(), entry.getValue());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求
        //当然netty还支持单线程、多线程网络IO模型，可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory = new NameThreadFactory();

        //方法返回到Java虚拟机的可用的处理器数量
        int parallel = Runtime.getRuntime().availableProcessors() * 2;
        System.out.println("可用处理器的数目为："+ parallel);

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MessageRecvChannelInitializer(concurrentHashMap).buildRpcSerializeProtocol(serializeProtocol))
                    .option(ChannelOption.SO_BACKLOG,128) // 设置TCP属性
                    .childOption(ChannelOption.SO_KEEPALIVE,true); //配置accepted的channel属性

            String[] ipAddr = serverAddress.split(DELIMITER);

            if(ipAddr.length==2){
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);
                ChannelFuture future = bootstrap.bind(host,port).sync();
                System.out.printf("[author loring] Netty RPC Server start success ip:%s port:%d\n", host, port);
                future.channel().closeFuture().sync();
            }else {
                System.out.printf("[author loring] Netty RPC Server start fail!\n");
            }
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
