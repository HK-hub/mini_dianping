package com.hk.remark.service.util;

import cn.hutool.core.lang.UUID;
import com.hk.remark.common.constants.RedisConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : HK意境
 * @ClassName : SimpleRedisLock
 * @date : 2022/11/2 9:51
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class SimpleRedisLock implements ILock {

    // 分布式锁名称
    private String lockName;

    // redis template
    private StringRedisTemplate redisTemplate;

    // lock 标识符
    private final String lockOwner = UUID.fastUUID().toString(true) + "-";

    // redis lua 脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    // 初始化 lua 脚本
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        // 指定 lua 脚本
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("lock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    public SimpleRedisLock(String lockName, StringRedisTemplate redisTemplate) {
        this.lockName = lockName;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 加锁
     *
     * @param timeOut 获取锁超时时间
     *
     * @return
     */
    @Override
    public boolean tryLock(long timeOut) {

        // 使用线程 id 作为 value
        String thread = lockOwner + Thread.currentThread().getId();
        // 获取锁
        Boolean lock = this.redisTemplate.opsForValue()
                .setIfAbsent(RedisConstants.LOCK_PREFIX + lockName, thread, timeOut, TimeUnit.SECONDS);

        return BooleanUtils.isTrue(lock);
    }


    /**
     * @return boolean
     *
     * @methodName : unlock
     * @author : HK意境
     * @date : 2022/11/2 10:01
     * @description :
     * @Todo :
     * @apiNote :
     * @params :
     * @throws:
     * @Bug : 可能存在锁被误删的情况, 释放锁原子性问题
     * @Modified : 唯一标识作为value, lua 脚本原子性执行
     * @Version : 1.0.0
     */
    @Override
    public boolean unlock() {

        // 线程标识和锁key
        Long result = redisTemplate.execute(UNLOCK_SCRIPT,
                List.of(RedisConstants.LOCK_PREFIX + this.lockName),
                lockOwner + Thread.currentThread().getId());

        // 删除成功返回1，失败返回0
        return !Objects.equals(result,0L);
    }
}
