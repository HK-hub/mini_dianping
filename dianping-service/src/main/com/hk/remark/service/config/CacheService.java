package com.hk.remark.service.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.entity.VoucherOrderPO;
import com.hk.remark.service.impl.SeckillVoucherServiceImpl;
import com.hk.remark.service.util.RedisDBChangeUtils;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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

    // 订单消息消费flag
    public static Boolean enableOrderConsume = Boolean.TRUE;


    static {
        createThreadPool();
    }


    // 初始化线程池
    public static void createThreadPool() {

        // 线程池参数计算: core=cpu核心数+1
        int core = Runtime.getRuntime().availableProcessors() + 1;
        // 最大线程数
        int maxSize = core;
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
        // 线程工厂
        cacheThreadPoolExecutor.setThreadFactory(r -> new Thread(r, RedisConstants.CACHE_THREAD_POOL + THREAD_COUNTER.getAndIncrement()));
    }

}
