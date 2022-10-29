package com.hk.remark.service.util;

import com.hk.remark.common.constants.RedisConstants;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author : HK意境
 * @ClassName : RedisDBChangeUtils
 * @date : 2022/10/28 21:43
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class RedisDBChangeUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate redisTemplate;

    public Boolean setDatabase(RedisTemplate redisTemplate, int num) {
        // 获取连接
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();

        // 切换数据源
        if (Objects.isNull(connectionFactory) || Objects.equals(num, connectionFactory.getDatabase())) {
            // 当前连接为 null 或者 当前数据库已经是目标数据库
            return Boolean.FALSE;
        }

        connectionFactory.setDatabase(num);
        redisTemplate.setConnectionFactory(connectionFactory);
        connectionFactory.resetConnection();

        return Boolean.TRUE;
    }


    /**
     * 指定db 切换连接
     * @param num
     * @return
     */
    public StringRedisTemplate getStringRedisTemplate(Integer num){

        // 计算下标位置
        int index = getIndex(num);
        // 设置选择的 db, 重新连接
        this.setDatabase(this.stringRedisTemplate, index);
        return this.stringRedisTemplate ;
    }
    public RedisTemplate getRedisTemplate(Integer num){

        // 计算下标位置
        int index = getIndex(num);
        // 设置选择的 db, 重新连接
        this.setDatabase(this.redisTemplate, index);
        return this.redisTemplate ;
    }


    /**
     * 根据 topic 进行分库
     * @param topic
     * @return
     */
    public StringRedisTemplate getStringRedisTemplate(String topic){

        // 计算下标位置
        int index = getIndex(topic);
        // 设置选择的 db, 重新连接
        this.setDatabase(this.stringRedisTemplate,index);
        return this.stringRedisTemplate ;
    }

    public RedisTemplate getRedisTemplate(String topic){

        // 计算下标位置
        int index = getIndex(topic);
        // 设置选择的 db, 重新连接
        this.setDatabase(this.redisTemplate,index);
        return this.redisTemplate ;
    }

    /**
     * 根据主题分库
     * @param topic
     * @return
     */
    public static int getIndex(String topic) {

        // 根据 hashCode 进行分发
        int hashCode = topic.hashCode();
        return hashCode % RedisConstants.DATABASE_NUMBER;
    }


    // 指定数据库
    public static int getIndex(int num) {
        return num % RedisConstants.DATABASE_NUMBER;
    }




}
