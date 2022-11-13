package com.hk.remark.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.entity.SeckillVoucherPO;
import com.hk.remark.entity.VoucherOrderPO;
import com.hk.remark.mapper.VoucherOrderMapper;
import com.hk.remark.service.ISeckillVoucherService;
import com.hk.remark.service.IVoucherOrderService;
import com.hk.remark.service.config.CacheService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.service.util.RedisIdWorker;
import com.hk.remark.vo.UserVO;
import org.apache.commons.lang3.BooleanUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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

    // 秒杀脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    public void initialize() {
        // 提交 订单消息消费任务
        // CacheService.cacheThreadPoolExecutor.submit(new VoucherOrderServiceImpl.VoucherOrderHandler());

    }

    @Resource
    private TransactionTemplate transactionTemplate ;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;
    @Resource
    private RedissonClient redissonClient;


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
    public ResponseResult seckillVoucher(Long voucherId) throws InterruptedException {

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
        String lockName = "order:voucher:" + userId + "." + voucherId;
        // 创建锁对象
        /*SimpleRedisLock simpleRedisLock = new SimpleRedisLock("order:voucher:" + userId + "." + voucherId,
                redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOCK_PREFIX));*/

        // 加锁
        //boolean isLock = simpleRedisLock.tryLock(10);
        // redisson 获取分布式锁
        RLock lock = redissonClient.getLock(lockName);
        // 获取锁
        boolean isLock = lock.tryLock(2, 10, TimeUnit.SECONDS);

        if (BooleanUtils.isFalse(isLock)) {
            // 加锁失败, 已经有抢购了抢购
            return ResponseResult.FAIL("亲,请勿重复下单哦");
        }

        /*synchronized (userId.toString().intern()) {
            // 获取代理对象(事务)
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(seckillVoucherPO);
        }*/

        try {
            // 获取代理对象(事务)
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            ResponseResult result = proxy.createVoucherOrder(seckillVoucherPO);
            return result;
        } finally {
            lock.unlock();
        }
    }


    /**
     * 异步秒杀
     *
     * @param voucherId
     *
     * @return
     */
    @Override
    public ResponseResult seckillVoucherAsync(Long voucherId) {

        // 执行 lua 脚本
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.SECKILL_STOCK_KEY);
        UserVO userVO = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        String userId = userVO.getId().toString();
        long orderId = redisIdWorker.nextId("order");

        // 执行脚本: 下单，发送下单消息
        Long result = redisTemplate.execute(SECKILL_SCRIPT, List.of(),
                String.valueOf(voucherId), userId, String.valueOf(orderId));

        // 判断返回结果
        if (!Objects.equals(result, 0L)) {
            return ResponseResult.FAIL(result == 1L ? "库存不足" : "请勿重复下单");
        }

        // 下单成功,发送下单成功消息
        return ResponseResult.SUCCESS(orderId);
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
        boolean update = updateChainWrapper.eq(SeckillVoucherPO::getVoucherId, seckillVoucherPO.getVoucherId()).gt(SeckillVoucherPO::getStock, 0).set(SeckillVoucherPO::getStock, seckillVoucherPO.getStock() - 1).update();

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
     * 订单消息消费
     * @param voucherOrder
     * @return
     */
    @Override
    public ResponseResult handleVoucherOrder(VoucherOrderPO voucherOrder) {

        if (Objects.isNull(voucherOrder)) {
            // 订单参数有误
            return ResponseResult.FAIL("创建订单失败");
        }

        // 编程式事务
        ResponseResult result = transactionTemplate.execute(transactionStatus -> {
            try{
                // 查询 seckill voucher
                SeckillVoucherPO seckillVoucherPO = seckillVoucherService.lambdaQuery()
                        .eq(SeckillVoucherPO::getVoucherId, voucherOrder.getVoucherId()).one();
                // 判断优惠卷信息
                if (Objects.isNull(seckillVoucherPO)) {
                    // 优惠卷不存在
                    return ResponseResult.FAIL("优惠卷不存在");
                }
                // 响应下单情况
                return createVoucherOrder(seckillVoucherPO);
            }catch(Exception e){
                transactionStatus.setRollbackOnly();
                return ResponseResult.FAIL("抢购失败!");
            }
        });

        return result;
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

    /**
     * 订单处理类
     */
    private class VoucherOrderHandler implements Callable<VoucherOrderPO> {

        private static final String queueName = "stream.orders";
        private final StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.SECKILL_STOCK_KEY);

        /**
         * 消费 订单消息
         *
         * @return
         *
         * @throws Exception
         */
        @Override
        public VoucherOrderPO call() throws Exception {

            // 持续不断的消费消息
            while (CacheService.enableOrderConsume) {
                try {
                    // 消费消息
                    this.consumeVoucherOrderMessage();
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    // 消息消费失败，没有被 ack, 进入 pending list
                    handlePendingList();
                    try {
                        TimeUnit.SECONDS.sleep(0);
                    }catch (InterruptedException exception){
                        exception.printStackTrace();
                    }
                }
            }

            return null;
        }


        /**
         * pending list 消息处理
         */
        private void handlePendingList() {

            while (CacheService.enableOrderConsume) {
                try{
                    // 1. 获取消息队列中的订单信息: xread group g1 c1 count 1 block 2000 streams stream.orders 0
                    List<MapRecord<String, Object, Object>> recordList = redisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    // 2. 判断消息获取是否成功
                    if (CollectionUtil.isEmpty(recordList)) {
                        // pending list 等待队列里面没有消息可以消费
                        break;
                    }

                    // 获取订单消息成功，处理订单
                    // 解析消息中的订单信息
                    MapRecord<String, Object, Object> record = recordList.get(0);
                    VoucherOrderPO voucherOrderPO = BeanUtil.fillBeanWithMap(record.getValue(), new VoucherOrderPO(), Boolean.TRUE);

                    // 消费消息
                    handleVoucherOrder(voucherOrderPO);

                    // 消费成功
                    // ACK 确认
                    redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                }catch(Exception e){
                    log.error("处理pending list 订单异常", e);
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    }catch (InterruptedException exception){
                        exception.printStackTrace();
                    }
                }
            }
        }

        /**
         *
         */
        private void consumeVoucherOrderMessage() {

            // 1. 获取消息队列中的订单信息: xread group g1 c1 count 1 block 2000 streams stream.orders >
            List<MapRecord<String, Object, Object>> recordList = redisTemplate.opsForStream().read(
                    Consumer.from("g1", "c1"),
                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                    StreamOffset.create(queueName, ReadOffset.lastConsumed())
            );

            // 2. 判断消息获取是否成功
            if (CollectionUtil.isEmpty(recordList)) {
                // 获取订单消息失败，sleep(0) 后继续获取消息
                return;
            }

            // 获取订单消息成功，处理订单
            // 解析消息中的订单信息
            MapRecord<String, Object, Object> record = recordList.get(0);
            VoucherOrderPO voucherOrderPO = BeanUtil.fillBeanWithMap(record.getValue(), new VoucherOrderPO(), Boolean.TRUE);

            // 消费消息
            handleVoucherOrder(voucherOrderPO);

            // 消费成功
            // ACK 确认
            redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

        }


    }




}
