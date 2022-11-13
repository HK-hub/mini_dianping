package com.hk.remark.service;

import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.SeckillVoucherPO;
import com.hk.remark.entity.VoucherOrderPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrderPO> {

    // 秒杀优惠卷
    ResponseResult seckillVoucher(Long voucherId) throws InterruptedException;

    // 异步秒杀
    ResponseResult seckillVoucherAsync(Long voucherId);


    ResponseResult createVoucherOrder(SeckillVoucherPO seckillVoucherPO);

    // 下单成功消息消费
    ResponseResult handleVoucherOrder(VoucherOrderPO voucherOrder);

}
