package com.hk.remark.web.config;

import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.entity.ShopPO;
import com.hk.remark.service.IShopService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : HK意境
 * @ClassName : WarmCacheConfig
 * @date : 2022/10/30 21:21
 * @description : 缓存预热配置
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Configuration
public class WarmCacheConfig {

    @Resource
    private IShopService shopService;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;

    @PostConstruct
    public void warmCacheEntrance(){

        // shopService.warmShopListCache();
        // 查询店铺信息，转换为 GEO 数据
        warmCacheShopGeo();
    }

    /**
     * 店铺 geo 数据
     */
    public void warmCacheShopGeo() {

        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.SHOP_GEO_KEY);
        // 以 商铺 type 为 key
        List<ShopPO> shopList = this.shopService.list();
        Map<Long, List<ShopPO>> typedShopList = shopList.stream().collect(Collectors.groupingBy(ShopPO::getTypeId));

        // 封装商铺地理位置 GEO 数据
        typedShopList.forEach((key, shops) -> {
            List<RedisGeoCommands.GeoLocation<String>> locationList = shops.stream()
                    .map(shop -> {
                        // 构造商铺地理位置对象
                        return new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),
                                new Point(shop.getX(), shop.getY()));
                    }).collect(Collectors.toList());

            // 保存进入 redis
            redisTemplate.opsForGeo().add(key.toString(), locationList);
        });

    }



}
