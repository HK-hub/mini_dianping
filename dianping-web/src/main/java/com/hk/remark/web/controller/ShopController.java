package com.hk.remark.web.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hk.remark.common.error.ApiException;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.SystemConstants;
import com.hk.remark.entity.ShopPO;
import com.hk.remark.service.IShopService;
import com.hk.remark.vo.ShopVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : ShopController
 * @author : HK意境
 * @date : 2022/10/26 16:31
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/{id}")
    public ResponseResult<ShopPO> queryShopById(@PathVariable("id") Long id) throws ApiException {

        ResponseResult result = this.shopService.queryById(id);

        return result;
    }

    /**
     * 新增商铺信息
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping
    public ResponseResult<Long> saveShop(@RequestBody ShopPO shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return ResponseResult.SUCCESS(shop.getId());
    }

    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return ResponseResult
     */
    @PutMapping
    public ResponseResult updateShop(@RequestBody ShopVO shop) {

        // 更新商铺信息
        ResponseResult result = this.shopService.updateShop(shop);

        // 响应结果
        return result;
    }

    /**
     * 根据商铺类型分页查询商铺信息
     * @param typeId 商铺类型
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/type")
    public ResponseResult<List<ShopPO>> queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据类型分页查询
        Page<ShopPO> page = shopService.query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return ResponseResult.SUCCESS(page.getRecords());
    }

    /**
     * 根据商铺名称关键字分页查询商铺信息
     * @param name 商铺名称关键字
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/name")
    public ResponseResult<List<ShopPO>> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<ShopPO> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return ResponseResult.SUCCESS(page.getRecords());
    }
}
