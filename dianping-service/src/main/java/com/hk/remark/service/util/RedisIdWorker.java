package com.hk.remark.service.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author : HK意境
 * @ClassName : RedisIdWorker
 * @date : 2022/11/1 16:37
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class RedisIdWorker {

    // 开始时间戳
    public static final long BEGIN_TIMESTAMP = 1640995200L;
    // 序列号长度
    public static final long COUNT_BITS = 32;
    // redis 自增
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * @methodName : nextId
     * @author : HK意境
     * @date : 2022/11/1 17:09
     * @description :
     * @Todo : 4 秒 生成 30000 id,
     * @apiNote :
     * @params :
         * @param keyPrefix 自增业务前缀
     * @return long
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    public long nextId(String keyPrefix){

        // 生成时间戳
        long current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = current - BEGIN_TIMESTAMP;

        // 生成序列号
        // 获取当前日期，精确到天
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        // 自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 拼接
        return timestamp << COUNT_BITS | count;
    }


}
