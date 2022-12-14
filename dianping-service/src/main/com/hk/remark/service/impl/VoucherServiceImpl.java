package com.hk.remark.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.dto.Result;
import com.hk.remark.entity.SeckillVoucherPO;
import com.hk.remark.entity.VoucherPO;
import com.hk.remark.mapper.VoucherMapper;
import com.hk.remark.service.ISeckillVoucherService;
import com.hk.remark.service.IVoucherService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, VoucherPO> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<VoucherPO> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(VoucherPO voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucherPO seckillVoucher = new SeckillVoucherPO();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        // 秒杀优惠卷放入 redis, 以便后续判断
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.SECKILL_STOCK_KEY);
        redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY+voucher.getId(),voucher.getStock().toString());

    }
}
