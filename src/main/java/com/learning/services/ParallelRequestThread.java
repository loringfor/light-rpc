package com.learning.services;

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
            AddCalculate addCalculate = (AddCalculate) sendProxy.getProxy(AddCalculate.class);
            MultiCalculate multiCalculate = (MultiCalculate)sendProxy.getProxy(MultiCalculate.class);
            int add = addCalculate.add(taskNumber,taskNumber);
            int multi = multiCalculate.multi(taskNumber,taskNumber);
            System.out.println("calc add result:[" + add + "]");
            System.out.println("calc multi result:[" + multi + "]");
            finish.countDown();
        }catch (InterruptedException ex){
            System.out.println("InterruptedException");
        }
    }
}
