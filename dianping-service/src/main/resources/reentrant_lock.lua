-- redisson 可重入锁实现原理
local key = KEYS[1];    -- 锁的key
local threadId = ARGV[1]    -- 线程唯一标识
local releaseTime = ARGV[2] -- 锁的自动释放时间

-- 判断锁是否存在
if (redis.call('exists', key) == 0) then
    -- 分布式锁不存在
    -- 加锁
    redis.call('hset', key, threadId, '1')
    -- 设置锁过期时间
    redis.call('expire', key, releaseTime);
    -- 加锁成功
    return 1;
end

-- 分布式锁存在
-- 判断锁持有者是否是当前线程
if (redis.call('hexists', key, threadId) == 1) then
    -- 当前锁持有者为当前线程
    -- 执行可重入操作,重入次数+1
    redis.call('hincryby', key, threadId, '1');
    -- 延续生存时间
    redis.call('expire', key, releaseTime);
end

-- 加锁失败:不是自己的锁
return 0;