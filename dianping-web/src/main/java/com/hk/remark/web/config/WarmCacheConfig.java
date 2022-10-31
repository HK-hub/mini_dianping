package com.hk.remark.web.config;

import com.hk.remark.service.IShopService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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

    @PostConstruct
    public void warmCacheEntrance(){

        // shopService.warmShopListCache();
    }


}
