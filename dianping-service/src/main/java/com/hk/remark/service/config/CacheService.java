package com.hk.remark.service.config;

import com.hk.remark.common.constants.RedisConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : HK意境
 * @ClassName : CacheService
 * @date : 2022/10/30 23:35
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class CacheService {

    // 线程计数
    public static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    public static ThreadPoolExecutor cacheThreadPoolExecutor = null;

    @PostConstruct
    public void initialize(){

        // 线程池参数计算: core=cpu核心数+1
        int core = Runtime.getRuntime().availableProcessors()+1;
        // 最大线程数
        int maxSize = core * 2;
        // 救急线程保活时间
        long keepAliveTime = 2L;

        // 创建线程池
        cacheThreadPoolExecutor = new ThreadPoolExecutor(
                core,
                maxSize,
                keepAliveTime,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        cacheThreadPoolExecutor.setThreadFactory(r -> {
            Thread thread = new Thread(r, RedisConstants.CACHE_THREAD_POOL+THREAD_COUNTER.getAndIncrement());
            return thread;
        });

    }

}
