package com.hk.remark.service.impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.entity.SeckillVoucherPO;
import com.hk.remark.entity.VoucherOrderPO;
import com.hk.remark.mapper.VoucherOrderMapper;
import com.hk.remark.service.ISeckillVoucherService;
import com.hk.remark.service.IVoucherOrderService;
import com.hk.remark.service.util.RedisIdWorker;
import com.hk.remark.vo.UserVO;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author : HK意境
 * @ClassName : VoucherOrderServiceImpl
 * @date : 2022/11/1 19:51
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrderPO> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;


    /**
     * @param voucherId 优惠卷id
     *
     * @return ResponseResult
     *
     * @methodName : seckillVoucher
     * @author : HK意境
     * @date : 2022/11/1 19:17
     * @description : 抢购优惠劵，创建订单
     * @Todo :
     * @apiNote : 抢购优惠劵，创建订单
     * @params :
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult seckillVoucher(Long voucherId) {

        // 一人一单
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        Integer userOrderCount = this.lambdaQuery().eq(VoucherOrderPO::getUserId, user.getId()).eq(VoucherOrderPO::getVoucherId, voucherId).count();

        // 用户下单历史判断
        if (userOrderCount > 0) {
            return ResponseResult.FAIL("亲,你已经参与过本次优惠活动了");
        }

        // 查询优惠卷
        SeckillVoucherPO seckillVoucherPO = this.seckillVoucherService.getById(voucherId);
        if (Objects.isNull(seckillVoucherPO)) {
            // 优惠券不存在
            return ResponseResult.FAIL("亲,优惠卷已经被抢完了,看看其他店铺吧");
        }

        // 判断秒杀是否在时间段内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillVoucherPO.getBeginTime()) || now.isAfter(seckillVoucherPO.getEndTime())) {
            // 秒杀时间未开始 or 秒杀时间已经结束
            return ResponseResult.FAIL("亲,当前时间不在秒杀活动范围哦");
        }

        // 判断库存是否充足
        if (seckillVoucherPO.getStock() < 1) {
            // 库存不足
            return ResponseResult.FAIL("对不起,手慢了,优惠券已经被抢光了");
        }

        // 对每一个 user 上锁
        Long userId = user.getId();
        synchronized (userId.toString().intern()) {
            // 获取代理对象(事务)
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(seckillVoucherPO);
        }
    }


    /**
     * @param seckillVoucherPO
     *
     * @return ResponseResult
     *
     * @methodName : createVoucherOrder
     * @author : HK意境
     * @date : 2022/11/1 20:54
     * @description : 创建订单
     * @Todo :
     * @apiNote :
     * @params :
     * @throws:
     * @Bug : 此处不能加 @Transactional 注解，因为会事务失效
     * @Modified :
     * @Version : 1.0.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult createVoucherOrder(SeckillVoucherPO seckillVoucherPO) {

        // 一人一单
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        Long userId = user.getId();

        Integer userOrderCount = this.lambdaQuery().eq(VoucherOrderPO::getUserId, userId).eq(VoucherOrderPO::getVoucherId, seckillVoucherPO.getVoucherId()).count();

        // 用户下单历史判断
        if (userOrderCount > 0) {
            return ResponseResult.FAIL("亲,你已经参与过本次优惠活动了");
        }

        // 扣减库存
        LambdaUpdateChainWrapper<SeckillVoucherPO> updateChainWrapper = this.seckillVoucherService.lambdaUpdate();
        boolean update = updateChainWrapper.eq(SeckillVoucherPO::getVoucherId, seckillVoucherPO.getVoucherId())
                .gt(SeckillVoucherPO::getStock, 0)
                .set(SeckillVoucherPO::getStock, seckillVoucherPO.getStock() - 1).update();

        if (Objects.equals(update, Boolean.FALSE)) {
            // 扣减库存失败
            // 库存不足
            return ResponseResult.FAIL("对不起,手慢了,优惠券已经被抢光了");
        }

        // 创建订单
        VoucherOrderPO voucherOrder = this.buildVoucherOrderPO(seckillVoucherPO);
        this.save(voucherOrder);

        // 返回信息
        return ResponseResult.SUCCESS(voucherOrder);

    }


    /**
     * 创建秒杀优惠订单对象
     *
     * @param seckillVoucherPO
     *
     * @return
     */
    private VoucherOrderPO buildVoucherOrderPO(SeckillVoucherPO seckillVoucherPO) {

        VoucherOrderPO voucherOrderPO = new VoucherOrderPO();
        // 订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrderPO.setId(orderId);
        // 优惠卷id
        voucherOrderPO.setVoucherId(seckillVoucherPO.getVoucherId());
        // 订单创建用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        voucherOrderPO.setUserId(user.getId());
        // 订单创建时间
        voucherOrderPO.setCreateTime(LocalDateTime.now());

        return voucherOrderPO;
    }


}
