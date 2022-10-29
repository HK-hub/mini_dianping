package com.hk.remark.service.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hk.remark.common.constants.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Slf4j
@Component
public class RedisDBChangeUtils {

    //redis地址
    @Value("${spring.redis.host}")
    private String host;

    //redis端口号
    @Value("${spring.redis.port}")
    private int port;

    //默认数据库
    private int defaultDB = 0;

    //多个数据库集合
    @Value("${spring.redis.dbs}")
    private List<Integer> dbList;

    //RedisTemplate实例
    private static Map<Integer, RedisTemplate<String, Object>> redisTemplateMap = new HashMap<>();
    // StringRedisTemplate 实例
    private static Map<Integer, StringRedisTemplate> stringRedisTemplateMap = new HashMap<>();

    /**
     * 初始化 redis 连接池
     */
    @PostConstruct
    public void initRedisTemplate() {
        defaultDB = dbList.get(0);//设置默认数据库
        for (Integer db : dbList) {
            //存储多个RedisTemplate实例
            redisTemplateMap.put(db, redisTemplate(db));
            stringRedisTemplateMap.put(db,stringRedisTemplate(db));
        }
    }

    /**
     * 指定连接数据库
     * @param db
     * @return
     */
    public LettuceConnectionFactory redisConnection(int db) {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration();
        server.setHostName(host); // 指定地址
        server.setDatabase(db); // 指定数据库
        server.setPort(port); //指定端口
        LettuceConnectionFactory factory = new LettuceConnectionFactory(server);
        factory.afterPropertiesSet(); //刷新配置
        return factory;
    }

    //RedisTemplate模板
    public RedisTemplate<String, Object> redisTemplate(int db) {
        //为了开发方便，一般直接使用<String,Object>
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //设置连接
        template.setConnectionFactory(redisConnection(db));
        //Json序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //String的序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        //hash的key采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        //value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //hash序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    //RedisTemplate模板
    public StringRedisTemplate stringRedisTemplate(int db) {
        //为了开发方便，一般直接使用<String,Object>
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnection(db)); //设置连接

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 指定数据库进行切换
     * @param db  数据库索引
     * @return
     */
    public RedisTemplate<String, Object> getRedisTemplate(int db) {
        return redisTemplateMap.get(getIndex(db));
    }
    public StringRedisTemplate getStringRedisTemplate(int db) {
        return stringRedisTemplateMap.get(getIndex(db));
    }

    /**
     * 使用默认数据库
     *
     * @return
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplateMap.get(defaultDB);
    }
    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplateMap.get(defaultDB);
    }

    /**
     * 根据 topic 进行分库
     * @param topic
     * @return
     */
    public StringRedisTemplate getStringRedisTemplate(String topic){

        // 计算下标位置
        int index = getIndex(topic);
        StringRedisTemplate redisTemplate = stringRedisTemplateMap.get(index);
        if (Objects.isNull(redisTemplate)) {
            // 返回默认
            return stringRedisTemplateMap.get(defaultDB);
        }
        return redisTemplate;
    }

    public RedisTemplate<String, Object> getRedisTemplate(String topic){

        // 计算下标位置
        int index = getIndex(topic);
        RedisTemplate<String, Object> redisTemplate = redisTemplateMap.get(index);
        if (Objects.isNull(redisTemplate)) {
            // 返回默认
            return redisTemplateMap.get(defaultDB);
        }
        return redisTemplate;
    }

    /**
     * 根据主题分库
     * @param topic
     * @return
     */
    public static int getIndex(String topic) {

        // 根据 hashCode 进行分发
        int hashCode = topic.hashCode();
        if (hashCode <= 0) {
            hashCode = -hashCode;
        }
        log.info("topic={},index={}",topic, hashCode % RedisConstants.DATABASE_NUMBER);
        return hashCode % RedisConstants.DATABASE_NUMBER;
    }


    // 指定数据库
    public static int getIndex(int num) {
        return num % RedisConstants.DATABASE_NUMBER;
    }




}
