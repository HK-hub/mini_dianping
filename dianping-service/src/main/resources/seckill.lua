-- 参数列表：优惠卷key=prefix+id
local voucherId = ARGV[1]
-- 用户id
local userId = ARGV[2]
-- 订单id
local orderId = ARGV[3]

-- 数据key
-- 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 判断库存是否充足
if tonumber(redis.call('get', stockKey)) <= 0 then
    -- 库存不足
    return 1
end

-- 库存充足，判断用户是否下单
if redis.call('sismember', orderKey, userId) == 1 then
    -- 存在用户下单记录,重复下单
    return 2
end

-- 扣减库存，下单
redis.call('incryby', stockKey, -1)
redis.call('sadd', orderKey, userId)
-- 发送消息到队列: xadd stream.orders * k1 v1 k2 v2 ...
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)

return 0