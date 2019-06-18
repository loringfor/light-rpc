package com.learning.core.threadpool;

import java.util.concurrent.*;

public class RpcThreadPool {

    public static Executor getExecutor(int thread,int queues){
        return new ThreadPoolExecutor(thread,thread,0, TimeUnit.SECONDS,
                queues==0 ? new SynchronousQueue<Runnable>()
                        :(queues < 0 ? new LinkedBlockingQueue<Runnable>()
                        : new LinkedBlockingQueue<Runnable>(queues)),
                new NameThreadFactory(),new AbortPolicyWithReport());
    }
}
