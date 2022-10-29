package com.hk.remark.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.ShopPO;
import com.hk.remark.manager.IShopManager;
import com.hk.remark.mapper.ShopMapper;
import com.hk.remark.mapstruct.ShopMapStructure;
import com.hk.remark.service.IShopService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.vo.ShopVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopPO> implements IShopService {

    @Resource
    private IShopManager shopManager;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;

    /**
     * @methodName :
     * @author : HK意境
     * @date : 2022/10/29 20:39
     * @description :
     * @Todo : 使用布隆过滤器解决缓存穿透
     * @apiNote : 根据 店铺id 查询商铺数据
     * @params :
         * @param id 店铺id
     * @return ResponseResult
     * @throws:
     * @Bug : 先查 布隆过滤器
     * @Modified :
     * @Version : 1.0.0
     */
    @Override
    public ResponseResult queryById(Long id) {

        // 1. 从redis查询 商铺缓存数据
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_KEY);
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = redisTemplate.opsForValue().get(key);

        // 2. 判断是否存在
        // 是否为空数据
        if (Objects.equals(RedisConstants.EMPTY_DATA_STRING, shopJson)) {
            // 空数据
            return ResponseResult.SUCCESS("亲,您查询的店铺不存在");
        }
        // 非空数据且为有效数据
        if (StringUtils.isNotBlank(shopJson)) {
            // 数据存在且不为空数据->查询数据存在
            // 3. 存在返回数据
            return ResponseResult.SUCCESS(JSONUtil.toBean(shopJson, ShopVO.class));
        }

        // 4. 不存在，从db 查询数据
        List<ShopPO> shopPOList = this.shopManager.queryShops(this.lambdaQuery().eq(ShopPO::getId, id));
        if (CollectionUtil.isEmpty(shopPOList)) {
            // 5. db 数据不存在：缓存穿透-> 缓存空数据:10 分钟
            redisTemplate.opsForValue().set(key, RedisConstants.EMPTY_DATA_STRING, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return ResponseResult.SUCCESS("亲,您查询的店铺不存在");
        }

        // 5. 将数据写入redis: 热点数据缓存 30 分钟
        ShopVO shopVO = ShopMapStructure.INSTANCE.shopPO2ShopVO(shopPOList.get(0));
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopVO), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 6. 返回数据
        return ResponseResult.SUCCESS(shopVO);

    }
}
