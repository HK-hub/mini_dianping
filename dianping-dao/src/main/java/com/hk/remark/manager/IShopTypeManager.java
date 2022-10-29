package com.hk.remark.manager;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hk.remark.entity.ShopTypePO;

import java.util.List;

/**
 * @author : HK意境
 * @ClassName : IShopTypeManager
 * @date : 2022/10/29 21:28
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IShopTypeManager {

    List<ShopTypePO> queryShopTypeList(LambdaQueryChainWrapper<ShopTypePO> wrapper);
}
