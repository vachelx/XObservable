package com.vachel.observable;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XThreadPoolManager {
    private static ThreadPoolExecutor sThreadPool;

    public static synchronized ThreadPoolExecutor getThreadPool() {
        if (sThreadPool == null) {
            synchronized (XThreadPoolManager.class) {
                if (sThreadPool == null) {
                    int corePoolSize = Runtime.getRuntime().availableProcessors();
                    int maximumPoolSize = corePoolSize * 2 + 1;
                    long keepAliveTime = 0;
                    sThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingDeque<Runnable>(),
                            Executors.defaultThreadFactory());
                }
            }
        }
        return sThreadPool;
    }
}
