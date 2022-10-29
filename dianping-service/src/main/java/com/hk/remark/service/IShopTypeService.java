package com.hk.remark.service;

import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.ShopTypePO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @ClassName : IShopTypeService
 * @author : HK意境
 * @date : 2022/10/26 13:20
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IShopTypeService extends IService<ShopTypePO> {

    /**
     * 查询全部店铺类型
     * @return
     */
    ResponseResult queryShopTypes();
}
