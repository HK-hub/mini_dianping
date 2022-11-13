package com.hk.remark.web.controller;


import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("/list")
    public ResponseResult queryTypeList() {

        ResponseResult<List> result = this.typeService.queryShopTypes();
        return result;
    }
}
