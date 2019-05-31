package com.learning.core.threadpool;

import java.util.concurrent.ThreadFactory;

/**
 * 线程工厂
 */

public class NameThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        return thread;
    }
}
