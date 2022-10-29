package com.hk.remark.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.google.common.collect.Lists;
import com.hk.remark.entity.ShopPO;
import com.hk.remark.manager.IShopManager;
import com.hk.remark.mapper.ShopMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : ShopManagerImpl
 * @date : 2022/10/29 20:14
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class ShopManagerImpl implements IShopManager {

    @Resource
    private ShopMapper shopMapper;

    @Override
    public List<ShopPO> queryShops(LambdaQueryChainWrapper<ShopPO> wrapper) {

        // 根据查询条件，查询商品
        List<ShopPO> shopPOList = wrapper.list();
        if (CollectionUtil.isEmpty(shopPOList)) {
            shopPOList = Lists.newArrayList();
        }

        return shopPOList;
    }
}
