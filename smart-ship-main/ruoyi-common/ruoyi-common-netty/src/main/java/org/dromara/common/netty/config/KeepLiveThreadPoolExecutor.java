package org.dromara.common.netty.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理线程任务
 */
@Component
@Order(1)
public final class KeepLiveThreadPoolExecutor {

    /**
     * 线程池参数-线程池数量
     */
    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

    /**
     * 线程池参数-核心线程数量
     */
    private static final Integer CORE_POOL_SIZE = 50;

    /**
     * 线程池参数-最大线程数量
     */
    private static final Integer MAX_POOL_SIZE = 100;

    /**
     * 线程池参数-保活时间
     */
    private static final Integer KEEP_ALIVE_TIME = 5;

    /**
     * 线程池参数-线程数量
     */
    private static final Integer QUEUE_SIZE = 1000;


    /**
     * 创建线程池
     */
    public static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(CORE_POOL_SIZE,
        MAX_POOL_SIZE, KEEP_ALIVE_TIME,
        TimeUnit.MINUTES,
        new ArrayBlockingQueue<>(
            QUEUE_SIZE),
        new NVRThreadFactory(
            "KeepLiveThreadPool"));

    private KeepLiveThreadPoolExecutor() {
    }

    /**
     * 自定义线程初始化
     */
    private static final class NVRThreadFactory implements ThreadFactory {
        /**
         * 命名前缀
         */
        private final String namePrefix;

        private NVRThreadFactory(String namePrefix1) {
            this.namePrefix = namePrefix1;
        }

        /**
         * 创建线程
         *
         */
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + "-" + THREAD_NUM.getAndIncrement());
        }
    }


}
