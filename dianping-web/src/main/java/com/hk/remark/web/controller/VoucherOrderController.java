package com.hk.remark.web.controller;


import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.dto.Result;
import com.hk.remark.service.IVoucherOrderService;
import com.hk.remark.service.impl.VoucherOrderServiceImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName : VoucherOrderController
 * @author : HK意境
 * @date : 2022/10/27 19:09
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {


    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("/seckill/{id}")
    public ResponseResult seckillVoucher(@PathVariable("id") Long voucherId) {
        // 秒杀优惠卷
        ResponseResult result = this.voucherOrderService.seckillVoucher(voucherId);

        return result;
    }
}
