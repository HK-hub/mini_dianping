package com.hk.remark.common.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * @ClassName : RedisData
 * @author : HK意境
 * @date : 2022/10/30 20:54
 * @description : redis 逻辑过期解决 缓存击穿问题使用的  逻辑过期数据载体
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
@Accessors(chain = true)
public class RedisData<T> {
    private LocalDateTime expireTime;
    private T data;
}
