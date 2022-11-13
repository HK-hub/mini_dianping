package com.hk.remark.service.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.util.RedisData;
import com.hk.remark.service.config.CacheService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author : HK意境
 * @ClassName : CacheClient
 * @date : 2022/10/31 13:20
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class CacheClient<T> {

    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;


    /**
     * 向topic 下db 设置缓存字符串
     * @param topic 主题
     * @param data  数据
     * @param ttl   过期时间
     * @param timeUnit  时间单位
     * @return
     */
    public <T> String set(String topic, String key, T data, Long ttl, TimeUnit timeUnit){

        StringRedisTemplate redisTemplate = this.redisDBChangeUtils.getStringRedisTemplate(topic);
        // 序列化
        String jsonStr = JSONUtil.toJsonStr(data);
        // 设置缓存
        redisTemplate.opsForValue().set(key, jsonStr, ttl, timeUnit);

        return jsonStr;
    }

    public <T> String set(String key, T data, Long ttl, TimeUnit timeUnit) {
        StringRedisTemplate redisTemplate = this.redisDBChangeUtils.getStringRedisTemplate();
        // 序列化
        String jsonStr = JSONUtil.toJsonStr(data);
        // 设置缓存
        redisTemplate.opsForValue().set(key, jsonStr, ttl, timeUnit);

        return jsonStr;
    }


    /**
     * 逻辑过期
     * @param topic
     * @param key
     * @param data
     * @param ttl
     * @param timeUnit
     * @return
     */
    public <T> String setWithLogicExpire(String topic, String key, T data, Long ttl, TimeUnit timeUnit){

        StringRedisTemplate redisTemplate = this.redisDBChangeUtils.getStringRedisTemplate(topic);
        // 序列化
        RedisData<T> redisData = new RedisData<T>().setData(data)
                .setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(ttl)));
        String jsonStr = JSONUtil.toJsonStr(redisData);
        // 设置缓存
        redisTemplate.opsForValue().set(key, jsonStr);

        return jsonStr;
    }


    /**
     * 缓存穿透
     * @param topic
     * @param id
     * @param type
     * @param <R>
     * @param <ID>
     * @return
     */
    public <R, ID> R queryWithPassThrough(String topic, ID id, Class<R> type, Function<ID,R> dbFallBack,
                                          Long ttl, TimeUnit timeUnit){
        // key
        String key = topic + id;
        // 获取 redisTemplate
        StringRedisTemplate redisTemplate = this.redisDBChangeUtils.getStringRedisTemplate(topic);
        // 查询缓存数据
        String jsonStr = redisTemplate.opsForValue().get(key);

        // 判断数据是否存在
        if (Objects.equals(jsonStr, RedisConstants.EMPTY_DATA_STRING)) {
            // 空数据
            return null;
        }
        if (StringUtils.isNotEmpty(jsonStr)) {
            // 存在，有效
            return JSONUtil.toBean(jsonStr,type);
        }

        // 不存在缓存，无效空数据->重建缓存
        R data = dbFallBack.apply(id);
        // 判断是否缓存穿透
        if (Objects.isNull(data)) {
            // 不存在，缓存空值
            redisTemplate.opsForValue().set(key, RedisConstants.EMPTY_DATA_STRING, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // db 数据查询成功,重建缓存
        this.set(topic, key, data, ttl, timeUnit);

        return data;
    }


    /**
     * 缓存击穿解决方案——逻辑过期
     * @param id
     * @return
     */
    public  <R,ID> R queryWithLogicExpire(String topic, String lockTopic, ID id, Class<R> type,Function<ID,R> dbFallback,
                                          Long ttl, TimeUnit timeUnit) {

        // 首先从缓存查询数据
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(topic);
        String key = topic + id;
        String jsonString = redisTemplate.opsForValue().get(key);

        if (StringUtils.isEmpty(jsonString) || Objects.equals(jsonString,RedisConstants.EMPTY_DATA_STRING)) {
            // 缓存数据未命中,说明不是热点数据
            return null;
        }

        // 判断缓存数据是否过期
        RedisData redisData = JSONUtil.toBean(jsonString, RedisData.class);
        R data = JSONUtil.toBean((JSONObject) redisData.getData(), type);

        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            // 缓存数据没过期,返回缓存对象
            return data;
        }

        // 缓存数据过期: 获取互斥锁，开辟异步线程更新缓存
        boolean lock = this.tryLock(lockTopic + id);
        if (Objects.equals(lock,Boolean.TRUE)) {
            // 获取锁成功，开启异步线程更新缓存
            // 重建缓存
            CacheService.cacheThreadPoolExecutor.submit(() -> {
                try{
                    // 查询数据库
                    R dto = dbFallback.apply(id);
                    // 写入缓存
                    setWithLogicExpire(topic, key, dto, ttl, timeUnit);
                    return dto;
                }catch(Exception e){
                    throw new RuntimeException(e);
                }finally {
                    // 释放锁
                    unLock(lockTopic + id);
                }
            });
        }

        // 获取锁失败, 返回历史数据
        return data;
    }


    /**
     * 获取商铺互斥锁
     * @param lockKey
     * @return
     */
    public boolean tryLock(String lockKey){
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOCK_SHOP_KEY);
        // 设置锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(Boolean.TRUE),
                RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);

        return BooleanUtils.isTrue(lock);
    }


    /**
     * 释放商铺互斥锁
     * @param lockKey
     * @return
     */
    public boolean unLock(String lockKey){
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOCK_SHOP_KEY);
        // 释放锁
        Boolean unlock = redisTemplate.delete(lockKey);

        return BooleanUtils.isTrue(unlock);
    }


}
