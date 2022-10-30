package com.hk.remark.manager;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.hk.remark.entity.ShopPO;

import java.util.List;

/**
 * @author : HK意境
 * @ClassName : IShopManager
 * @date : 2022/10/29 20:13
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IShopManager {

    List<ShopPO> queryShops(LambdaQueryChainWrapper<ShopPO> wrapper);

    // 更新
    Boolean updateShopPO(LambdaUpdateChainWrapper<ShopPO> lambdaUpdate, ShopPO shopPO);
}
