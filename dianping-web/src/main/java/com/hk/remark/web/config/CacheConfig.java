package com.hk.remark.web.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : HK意境
 * @ClassName : CacheConfig
 * @date : 2022/10/29 14:03
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedissonClient redissonClient(){

        // redis 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");

        // 创建redisson 客户端
        return Redisson.create(config);
    }


}
