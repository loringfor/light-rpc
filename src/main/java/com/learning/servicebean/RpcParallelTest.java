package com.learning.servicebean;

import com.learning.core.RpcServerLoader;
import com.learning.core.send.MessageSendCGlibProxy;
import com.learning.core.send.MessageSendJDKProxy;
import com.learning.serialize.RpcSerializeProtocol;
import org.apache.commons.lang.time.StopWatch;

import java.util.concurrent.CountDownLatch;

public class RpcParallelTest {
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "127.0.0.1:18888";
        RpcServerLoader loader = RpcServerLoader.getInstance();
        RpcSerializeProtocol protocol =RpcSerializeProtocol.PROTOSTUFFSERIALIZE;
        loader.load(serverAddress,protocol);

        // JDK和CGLib动态代理都可以使用，CGLib的效率高于JDK
//        MessageSendJDKProxy sendProxy = new MessageSendJDKProxy();
        MessageSendCGlibProxy sendProxy = new MessageSendCGlibProxy();
        //并行度
        int parallel=5000;

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
