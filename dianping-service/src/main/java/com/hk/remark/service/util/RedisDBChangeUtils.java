package com.hk.remark.web.util;

import com.hk.remark.common.constants.RedisConstants;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
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
    private StringRedisTemplate redisTemplate;

    public Boolean setDatabase(int num) {
        // 获取连接
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();

        // 切换数据源
        if (Objects.isNull(connectionFactory) || Objects.equals(num, connectionFactory.getDatabase())) {
            // 当前连接为 null 或者 当前数据库已经是目标数据库
            return Boolean.FALSE;
        }

        connectionFactory.setDatabase(num);
        this.redisTemplate.setConnectionFactory(connectionFactory);
        connectionFactory.resetConnection();

        return Boolean.TRUE;
    }


    public StringRedisTemplate getStringRedisTemplate(Integer num){

        // 如果 null 为空，返回 0db
        if (Objects.isNull(num) || Objects.equals(num,0)) {
            // 设置选择的 db, 重新连接
            this.setDatabase(0);
            return this.redisTemplate ;
        }

        // 计算下标位置

    }


    /**
     * 根据主题分库
     * @param topic
     * @return
     */
    public static int getIndex(String topic){

        int code = topic.hashCode();
        

    }


    // 指定数据库
    public static int getIndex(int num){
        return num % RedisConstants.DATABASE_NUMBER ;
    }




}
