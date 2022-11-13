package com.hk.remark.service;

import com.hk.remark.common.error.ApiException;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.ShopPO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hk.remark.vo.ShopVO;

import java.util.List;

/**
 * @ClassName : IShopService
 * @author : HK意境
 * @date : 2022/10/26 13:22
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IShopService extends IService<ShopPO> {

    ResponseResult queryById(Long id) throws ApiException;

    // 根据 shopVO 对象跟新 PO
    ResponseResult updateShop(ShopVO shop);


    // 缓存预热
    List<ShopVO> warmShopListCache();


}