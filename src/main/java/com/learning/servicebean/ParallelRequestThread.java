package com.learning.servicebean;

import com.learning.core.send.MessageSendCGlibProxy;
import com.learning.core.send.MessageSendJDKProxy;

import java.util.concurrent.CountDownLatch;

public class ParallelRequestThread implements Runnable{
    private MessageSendJDKProxy sendProxy;
    private CountDownLatch signal;
    private CountDownLatch finish;
    private int taskNumber;

    public ParallelRequestThread(MessageSendJDKProxy sendProxy, CountDownLatch signal, CountDownLatch finish, int taskNumber) {
        this.sendProxy = sendProxy;
        this.signal = signal;
        this.finish = finish;
        this.taskNumber = taskNumber;
    }

    @Override
    public void run() {
        try{
            signal.await();
            // calculate为代理对象
            Calculate calculate = (Calculate) sendProxy.getProxy(Calculate.class);
            int add = calculate.add(taskNumber,taskNumber);
            System.out.println("calc add result:[" + add + "]");
            finish.countDown();
        }catch (InterruptedException ex){
            System.out.println("InterruptedException");
        }
    }
}
