package com.hk.remark.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.error.ApiException;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.resp.ResultCode;
import com.hk.remark.common.util.RedisData;
import com.hk.remark.entity.ShopPO;
import com.hk.remark.manager.IShopManager;
import com.hk.remark.mapper.ShopMapper;
import com.hk.remark.mapstruct.ShopMapStructure;
import com.hk.remark.service.IShopService;
import com.hk.remark.service.config.CacheService;
import com.hk.remark.service.util.CacheClient;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName : ShopServiceImpl
 * @author : HK意境
 * @date : 2022/10/30 21:24
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopPO> implements IShopService {

    @Resource
    private IShopManager shopManager;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;
    @Resource
    private CacheClient<ShopVO> cacheClient;

    /**
     * @methodName :queryById
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

        // 缓存穿透
        //ResponseResult result = this.queryWithPassThrough(id);
        // 工具类解决缓存穿透
        ShopVO shopVO = this.cacheClient.queryWithPassThrough(RedisConstants.CACHE_SHOP_KEY, id, ShopVO.class,
                (primaryKey) -> {
                    List<ShopPO> shopPOList = this.shopManager.queryShops(this.lambdaQuery().eq(ShopPO::getId, primaryKey));
                    if (CollectionUtil.isEmpty(shopPOList)) {
                        return null;
                    }
                    // 转换为 vo
                    return ShopMapStructure.INSTANCE.shopPO2ShopVO(shopPOList.get(0));
                },
                RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 缓存击穿-互斥锁
        //ResponseResult result = this.queryWithMutex(id);

        // 缓存击穿-逻辑过期
        // ResponseResult result = this.queryWithLogicExpire(id);
        /*this.cacheClient.queryWithLogicExpire(RedisConstants.CACHE_SHOP_KEY, RedisConstants.LOCK_SHOP_KEY, id, ShopVO.class,
                (primaryKey)->{
                    List<ShopPO> shopPOList = this.shopManager.queryShops(this.lambdaQuery().eq(ShopPO::getId, primaryKey));
                    if (CollectionUtil.isEmpty(shopPOList)) {
                        return null;
                    }
                    // 转换为 vo
                    return ShopMapStructure.INSTANCE.shopPO2ShopVO(shopPOList.get(0));
                },
                RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);*/

        // 判断商铺数据是否存在
        if (Objects.isNull(shopVO)) {
            return ResponseResult.FAIL("对不起亲,您查询的店铺不存在,请浏览其他");
        }

        return ResponseResult.SUCCESS(shopVO);
    }


    /**
     * 从redis 查询缓存商铺数据
     * @param id 商铺id
     * @return
     */
    private ResponseResult<ShopVO> getShopVOFromCache(Long id){

        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_KEY);
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = redisTemplate.opsForValue().get(key);

        // 2. 判断是否存在
        if (StringUtils.isEmpty(shopJson)) {
            // 缓存不存在
            return ResponseResult.FAIL();
        }

        // 是否为空数据
        if (Objects.equals(RedisConstants.EMPTY_DATA_STRING, shopJson)) {
            // 空数据
            return ResponseResult.FAIL();
        }

        // 缓存数据有效
        return ResponseResult.SUCCESS(JSONUtil.toBean(shopJson, ShopVO.class));

    }

    /**
     * 放入店铺数据进入缓存
     * @param shopPOList
     * @return
     */
    private ResponseResult<ShopVO> setShopVOToCache(List<ShopPO> shopPOList, Long id){
        // 获取对应数据库 redisTemplate
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_KEY);
        String key = RedisConstants.CACHE_SHOP_KEY + id;

        if (CollectionUtil.isEmpty(shopPOList)) {
            // 5. db 数据不存在：缓存穿透-> 缓存空数据:10 分钟
            redisTemplate.opsForValue().set(key, RedisConstants.EMPTY_DATA_STRING, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return ResponseResult.FAIL();
        }

        // 5. 将数据写入redis: 热点数据缓存 30 分钟
        ShopVO shopVO = ShopMapStructure.INSTANCE.shopPO2ShopVO(shopPOList.get(0));
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopVO), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return ResponseResult.SUCCESS(shopVO);
    }


    /**
     * 重建商铺信息缓存
     * @param fromCacheResult
     * @param id
     * @param tryLockResult
     * @return ResponseResult
     * @throws Exception
     */
    private ResponseResult rebuildShopVOCache(ResponseResult fromCacheResult,Long id,Boolean tryLockResult) throws Exception {

        // 互斥锁
        String lock = RedisConstants.LOCK_SHOP_KEY+id;

        if (BooleanUtils.isFalse(tryLockResult)) {
            // 4. 获取失败，休眠，继续自旋获取
            for (int i = 0; i < RedisConstants.CACHE_REBUILD_COUNT; i++) {
                tryLockResult = cacheClient.tryLock(lock);
                if (BooleanUtils.isTrue(tryLockResult)) {
                    // 获取锁成功
                    break;
                }
                // 休眠自旋，让出 cpu. 每次休眠 5 毫秒
                Thread.sleep(10);
            }

            // 获取锁失败: 重试十次都失败，说明竞争非常激烈，触发服务降级
            if (BooleanUtils.isFalse(tryLockResult)) {
                return new ResponseResult(ResultCode.SERVER_BUSY);
            }
        }

        // 3. 获取成功，查询数据库，设置缓存
        // double check 再次检查缓存中是否已经有缓存了
        fromCacheResult = this.getShopVOFromCache(id);
        if (Objects.equals(fromCacheResult.isSuccess(),Boolean.TRUE)) {
            // 缓存命中
            return fromCacheResult;
        }

        // 确认 double check 之后缓存中不存在数据->查询数据库重建缓存
        List<ShopPO> shopPOList = this.shopManager.queryShops(this.lambdaQuery().eq(ShopPO::getId, id));
        Thread.sleep(200);
        ResponseResult<ShopVO> rebuildCacheResult = this.setShopVOToCache(shopPOList,id);

        return rebuildCacheResult;
    }


    /**
     * 互斥锁解决缓存击穿: 热点key 问题 和 缓存数据重建复杂的业务数据
     * @param id
     * @return
     */
    private ResponseResult queryWithMutex(Long id){

        // 从缓存获取数据
        ResponseResult<ShopVO> fromCacheResult = ResponseResult.FAIL();
        fromCacheResult = this.getShopVOFromCache(id);
        if (Objects.equals(fromCacheResult.isSuccess(),Boolean.TRUE)) {
            // 缓存命中
            return fromCacheResult;
        }

        // 缓存 未命中->互斥重建缓存数据
        // 1. 获取互斥锁
        String lock = RedisConstants.LOCK_SHOP_KEY + id;

        // 获取锁重建缓存
        try{
            // 获取
            boolean tryLockResult = cacheClient.tryLock(lock);
            // 重建
            ResponseResult rebuildCacheResult = this.rebuildShopVOCache(fromCacheResult,id,tryLockResult);
            // 释放锁
            cacheClient.unLock(lock);
            // 返回数据
            return rebuildCacheResult;
        }catch(Exception e){
            e.printStackTrace();
            // 缓存重建失败:可能出现异常，可能缓存穿透
            return ResponseResult.FAIL("对不起，你查询的商铺不存在,请看看其他店铺吧");

        }finally {
            cacheClient.unLock(lock);
        }

    }


    /**
     * @methodName : updateShop
     * @author : HK意境
     * @date : 2022/10/30 12:49
     * @description : 根据 shopVO 跟新 商铺信息
     * @Todo : 采用延迟双删,采用编程式事务进行控制
     * @apiNote : 采用 cache aside 模式，先跟新数据库，再删除缓存
     * @params :
         * @param shopVO vo对象
     * @return ResponseResult
     * @throws:
     * @Bug : 先更新数据库，在删除缓存，可能出现数据不一致，缓存中被放入脏数据
     * @Modified :
     * @Version : 1.0.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateShop(ShopVO shopVO) {

        // 校验
        if (StringUtils.isEmpty(""+shopVO.getId())) {
            // id 为空
            return ResponseResult.FAIL("跟新失败,商铺不存在!");
        }

        // 先跟新数据库，在删除缓存
        // 转换实体数据类型
        ShopPO shopPO = ShopMapStructure.INSTANCE.shopVO2ShopPO(shopVO);
        // 更新数据库
        Boolean updated = this.shopManager.updateShopPO(
                this.lambdaUpdate().eq(ShopPO::getId,shopPO.getId()),shopPO);

        // 更新失败
        if (Objects.equals(updated,Boolean.FALSE)) {
            return new ResponseResult(ResultCode.SERVER_BUSY);
        }

        // 更新成功，删除缓存
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_KEY);
        redisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shopPO.getId());

        return ResponseResult.SUCCESS("更新商铺信息="+updated);
    }


    /**
     * 店铺信息进行缓存预热
     * @return
     */
    @Override
    public List<ShopVO> warmShopListCache(){

        log.info("shop cache data warm start:"+System.currentTimeMillis());

        // 查询满足条件的热点数据
        List<ShopPO> shopPOS = this.shopManager.queryShops(this.lambdaQuery());
        // 预热缓存
        List<ShopVO> shopVOS = shopPOS.stream().map(ShopMapStructure.INSTANCE::shopPO2ShopVO).collect(Collectors.toList());

        // 建立 redisData
        buildRedisDataOfShop(shopVOS);

        return shopVOS;
    }

    /**
     * 将 店铺信息转换为 redis热点数据 redisData 数据缓存
     * @param shopVOList
     * @return
     */
    private List<RedisData<ShopVO>> buildRedisDataOfShop(List<ShopVO> shopVOList){
        // 转换为 redisData
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_KEY);
        // 缓存redis 热点数据
        List<RedisData<ShopVO>> redisDataList = shopVOList.stream().map(vo -> {
            RedisData<ShopVO> shopVORedisData = new RedisData<ShopVO>().setData(vo)
                    .setExpireTime(LocalDateTime.now().plusSeconds(10));
            // 放入缓存
            redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + vo.getId(), JSONUtil.toJsonStr(shopVORedisData));
            return shopVORedisData;
        }).collect(Collectors.toList());

        return redisDataList;
    }


}
