package com.hk.remark.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.ShopTypePO;
import com.hk.remark.manager.IShopTypeManager;
import com.hk.remark.mapper.ShopTypeMapper;
import com.hk.remark.mapstruct.ShopTypeMapStructure;
import com.hk.remark.service.IShopTypeService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.vo.ShopTypeVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopTypePO> implements IShopTypeService {

    @Resource
    private IShopTypeManager shopTypeManager;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;

    @Override
    public ResponseResult queryShopTypes() {

        // 先查redis 缓存
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.CACHE_SHOP_TYPE_KEY);
        Set<String> typeSet = redisTemplate.opsForZSet().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);

        // 缓存数据存在判断
        if (CollectionUtil.isNotEmpty(typeSet)) {
            // 店铺类型缓存数据存在，返回
            List<ShopTypeVO> typeVOList = typeSet.stream().map((json) -> JSONUtil.toBean(json, ShopTypeVO.class))
                    .collect(Collectors.toList());
            return ResponseResult.SUCCESS(typeVOList);
        }

        // 商铺类型数据不存在,查询数据库
        // 店铺分类名称去重，根据 sort 排序后响应
        List<ShopTypePO> shopTypePOList = this.shopTypeManager.queryShopTypeList(this.lambdaQuery());

        // 转为 vo 对象
        List<ShopTypeVO> typeVOList = shopTypePOList.stream()
                .map(ShopTypeMapStructure.INSTANCE::shopTypePO2VO).collect(Collectors.toList());

        // 存放如 redis 中: key=cache:shop:type:, v=element, score=e.getSort()
        typeVOList.forEach(vo -> {
            // redisTemplate.executePipelined() 使用 pipeline
            redisTemplate.opsForZSet().add(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(vo), vo.getSort());
        });

        return ResponseResult.SUCCESS(typeVOList);
    }
}
