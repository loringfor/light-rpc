package com.learning.cluster;

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
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class FailOverClusterInvoker {
    ServiceDiscovery serviceDiscovery;
    int parallel;
    RpcSerializeProtocol protocol;
    //重试次数
    int retryTimes;
    public FailOverClusterInvoker(ServiceDiscovery serviceDiscovery, int parallel, RpcSerializeProtocol  protocol, int retryTimes){
        this.serviceDiscovery = serviceDiscovery;
        this.parallel = parallel;
        this.protocol = protocol;
        this.retryTimes = retryTimes;
    }
    public void invoke() throws NullPointerException{

        List<String> invokers;
        List<String> invoked = new ArrayList<>();
        RpcServerLoader loader;
        String serverAddress;

        for(int i = 0; i < retryTimes; i++) {
            invokers = serviceDiscovery.discoverAll();
            check(invokers);
            serverAddress = serviceDiscovery.discover();
            invoked.add(serverAddress);

            // 单例模式，只有一个loader对象
            loader = RpcServerLoader.getInstance();

            // 建立发送的线程并提交到线程池
//        loader.load(serverAddress,protocol);

            EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);
            String[] ipAddr = serverAddress.split(":");
            if (ipAddr.length == 3) {
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);
                InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(remoteAddr)
                        .handler(new MessageSendChannelInitializer().buildRpcSerializeProtocol(protocol));
                //连接到远程地址
                ChannelFuture channelFuture = bootstrap.connect(remoteAddr);
                RpcServerLoader finalLoader = loader;
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            MessageSendHandler handler = channelFuture.channel().pipeline().get(MessageSendHandler.class);
                            finalLoader.setMessageSendHandler(handler);
                        } else {
                            System.err.println("建立Netty远程连接错误");
                            channelFuture.cause().printStackTrace();
                        }
                    }
                });
            }
            // JDK和CGLib动态代理都可以使用，CGLib的效率高于JDK
            MessageSendJDKProxy sendProxy = new MessageSendJDKProxy();
//        MessageSendCGlibProxy sendProxy = new MessageSendCGlibProxy();

            AddCalculate addCalculate = (AddCalculate) sendProxy.getProxy(AddCalculate.class);
            int add = addCalculate.add(10, 15);
            if (add == -11111){
                System.out.printf("第%d次远程调用失败\n", (i+1));
            }
            else {
                System.out.println("calc add result:[" + add + "]");
                break;
            }
        }
//        loader.unLoad();
    }
    private  void check(List<String> invokers) throws NullPointerException{
        if (CollectionUtils.isEmpty(invokers)) {
            System.out.println("未发现可用服务器");
            throw new NullPointerException();
        }
    }
}
