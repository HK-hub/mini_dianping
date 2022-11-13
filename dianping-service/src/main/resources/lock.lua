-- 这里的 KEYS[1] 就是锁的key
-- 这里的 ARGV[1] 就是当前线程的标识

-- 比较当前线程标识和锁中线程标识
if (ARGV[1] == redis.call('get', KEYS[1])) then
    -- 当前线程是拥有者
    return redis.call('del', KEYS[1])
end
-- 当前线程不是拥有者
return 0;
