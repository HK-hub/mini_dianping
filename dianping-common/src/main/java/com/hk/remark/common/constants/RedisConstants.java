package com.hk.remark.common.constants;

/**
 * @author : HK意境
 * @ClassName : RedisConstants
 * @date : 2022/10/29 10:01
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class RedisConstants {

    // redis db 数量
    public static final int DATABASE_NUMBER = 10;
    // redis 空数据
    public static final String EMPTY_DATA_STRING = "empty:data";
    // redis 热点数据缓存时间: 24 小时
    public static final Long HOT_DATA_TTL = 24L;

    // 验证码缓存 key
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    // 用户token 缓存key
    public static final String LOGIN_USER_KEY = "login:token:";
    // 用户
    public static final String LOGIN_USER_MAP = "login:user:map";
    public static final Long LOGIN_USER_TTL = 36000L;

    // 空数据 缓存时间
    public static final Long CACHE_NULL_TTL = 2L;

    // 商铺 缓存时间
    public static final Long CACHE_SHOP_TTL = 30L;

    // 店铺缓存 key
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    // 店铺类型缓存 key
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";


    /**
     * 判断 key 值是否存在于 redis 中
     * @param key
     * @return
     */
    public static Boolean isExistsKey(String key){
        return Boolean.TRUE;
    }






}
