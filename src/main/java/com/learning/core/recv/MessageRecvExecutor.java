package com.learning.core.recv;

import com.learning.core.threadpool.NameThreadFactory;
import com.learning.core.threadpool.RpcThreadPool;
import com.learning.registry.ServiceRegistry;
import com.learning.serialize.RpcSerializeProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageRecvExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MessageRecvExecutor.class);
    private final String DELIMITER = ":";
    private String serverAddress;
    private String serverWeight;
    private ServiceRegistry serviceRegistry;
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
    public MessageRecvExecutor(String serverAddress, ServiceRegistry serviceRegistry, RpcSerializeProtocol serializeProtocol) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
        this.serializeProtocol = serializeProtocol;
    }
    public MessageRecvExecutor(String serverAddress, String serverWeight, ServiceRegistry serviceRegistry, RpcSerializeProtocol serializeProtocol) {
        this.serverAddress = serverAddress;
        this.serverWeight = serverWeight;
        this.serviceRegistry = serviceRegistry;
        this.serializeProtocol = serializeProtocol;
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

    public void start() throws Exception {
        //netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求
        //当然netty还支持单线程、多线程网络IO模型，可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory = new NameThreadFactory();

        //方法返回到Java虚拟机的可用的处理器数量
        int parallel = Runtime.getRuntime().availableProcessors() * 2;
        logger.info("可用处理器的数目为：{}",parallel);

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MessageRecvChannelInitializer(concurrentHashMap).buildRpcSerializeProtocol(serializeProtocol))
                    .option(ChannelOption.SO_BACKLOG,128) // 设置TCP属性
                    .childOption(ChannelOption.SO_KEEPALIVE,true); //配置accepted的channel属性

            String serverAddressAndWeight = serverAddress + ":" + serverWeight;
            String[] ipAddr = serverAddressAndWeight.split(DELIMITER);

            if(ipAddr.length==3){
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);
                ChannelFuture future = bootstrap.bind(host,port).sync();
                logger.info("[author Loring] Netty RPC Server start success in ip:{},port:{} ", host, port);

                if (serviceRegistry != null) {
                    serviceRegistry.register(serverAddressAndWeight); // 注册服务地址
                }

                future.channel().closeFuture().sync();
            }else {
                logger.error("[author loring] Netty RPC Server start fail!");
            }
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerWeight() {
        return serverWeight;
    }

    public void setServerWeight(String serverWeight) {
        this.serverWeight = serverWeight;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public RpcSerializeProtocol getSerializeProtocol() {
        return serializeProtocol;
    }

    public void setSerializeProtocol(RpcSerializeProtocol serializeProtocol) {
        this.serializeProtocol = serializeProtocol;
    }

    public Map<String, Object> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    public void setConcurrentHashMap(Map<String, Object> concurrentHashMap) {
        this.concurrentHashMap = concurrentHashMap;
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        try {
//            MessageKeyVal messageKeyVal = (MessageKeyVal) applicationContext.getBean(Class.forName("com.learning.model.MessageKeyVal"));
//            Map<String,Object> map = messageKeyVal.getMessageKeyVal();
//            Iterator iterator =map.entrySet().iterator();
//            while (iterator.hasNext()){
//                Map.Entry entry = (Map.Entry) iterator.next();
//                concurrentHashMap.put(entry.getKey().toString(), entry.getValue());
//                logger.info("Service put in HashMap, >>>key:{},>>>value:{}",entry.getKey().toString(),entry.getValue());
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//
//    }
}
