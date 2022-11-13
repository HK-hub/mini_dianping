package com.hk.remark.service.util;

/**
 * @author : HK意境
 * @ClassName : ILock
 * @date : 2022/11/2 9:47
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface ILock {

    /**
     * 尝试获取锁-分布式锁
     * @param timeOut 获取锁超时时间
     * @return Boolean
     */
    public boolean tryLock(long timeOut);


    public boolean unlock();

}
