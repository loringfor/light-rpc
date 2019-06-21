package com.learning;

import com.learning.core.RpcServerLoader;
import com.learning.core.send.MessageSendChannelInitializer;
import com.learning.core.send.MessageSendHandler;
import com.learning.core.send.MessageSendJDKProxy;
import com.learning.registry.ServiceDiscovery;
import com.learning.serialize.RpcSerializeProtocol;
import com.learning.services.AddCalculate;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;

/**
 * @author Loring
 * 测试单个实例
 */
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = null;
        // 127.0.0.1:2181为ZooKeepeer地址
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("192.168.1.127:2181");
        if(serviceDiscovery != null){
            serverAddress = serviceDiscovery.discover();
        }

        // 单例模式，只有一个loader对象
        RpcServerLoader loader = RpcServerLoader.getInstance();
        RpcSerializeProtocol protocol =RpcSerializeProtocol.PROTOSTUFFSERIALIZE;

        // 建立发送的线程并提交到线程池
//        loader.load(serverAddress,protocol);

        int parallel = Runtime.getRuntime().availableProcessors() * 2;
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);
        String[] ipAddr = serverAddress.split(":");
        if(ipAddr.length==3){
            String host = ipAddr[0];
            int port = Integer.parseInt(ipAddr[1]);
            InetSocketAddress remoteAddr = new InetSocketAddress(host,port);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(remoteAddr)
                    .handler(new MessageSendChannelInitializer().buildRpcSerializeProtocol(protocol));
            ChannelFuture channelFuture = bootstrap.connect(remoteAddr);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        MessageSendHandler handler = channelFuture.channel().pipeline().get(MessageSendHandler.class);
                        loader.setMessageSendHandler(handler);
                    }
                }
            });
        }

        // JDK和CGLib动态代理都可以使用，CGLib的效率高于JDK
        MessageSendJDKProxy sendProxy = new MessageSendJDKProxy();
        // MessageSendCGlibProxy sendProxy = new MessageSendCGlibProxy();

        AddCalculate addCalculate = (AddCalculate) sendProxy.getProxy(AddCalculate.class);
        int add = addCalculate.add(10,15);
        System.out.println("calc add result:[" + add + "]");

//        loader.unLoad();
    }
}
