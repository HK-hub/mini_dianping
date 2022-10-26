package com.hk.remark.service;

import com.hk.remark.dto.Result;
import com.hk.remark.entity.VoucherPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherService extends IService<VoucherPO> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(VoucherPO voucher);
}
