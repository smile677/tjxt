package com.tianji.promotion.utils;

import com.tianji.common.exceptions.BizIllegalException;
import org.redisson.api.RLock;

public enum MyLockStrategy {
    /**
     * 快速结束: 不重试FAST(boolean isLock=lock.tryLock(0,10,SECONDS)+直接结束SKIP(if(!isLock)return)
     */
    SKIP_FAST() {
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            return lock.tryLock(0, prop.leaseTime(), prop.unit());
        }
    },
    /**
     * 快速失败: 不重试FAST(boolean isLock=lock.tryLock(0,10,SECONDS)+抛出异常FAIL(if(!isLock)throw new Exception)
     */
    FAIL_FAST() {
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            boolean isLock = lock.tryLock(0, prop.leaseTime(), prop.unit());
            if (!isLock) {
                throw new BizIllegalException("请求太频繁");
            }
            return true;
        }
    },
    /**
     * 无限重试: 无限重试KEEP_TRYING(lock.lock(10,SECONDS))
     */
    KEEP_TRYING() {
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            lock.lock(prop.leaseTime(), prop.unit());
            return true;
        }
    },
    /**
     * 重试超时后结束: 有限重试(RETRY_TIMEOUT(boolean isLock=lock.tryLock(5,10,SECONDS)))+直接结束SKIP(if(!isLock)return)
     */
    SKIP_AFTER_RETRY_TIMEOUT() {
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            return lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
        }
    },
    /**
     * 重试超时后失败: 有限重试(RETRY_TIMEOUT(boolean isLock=lock.tryLock(5,10,SECONDS)))+抛出异常FAIL(if(!isLock)throw new Exception)
     */
    FAIL_AFTER_RETRY_TIMEOUT() {
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            boolean isLock = lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
            if (!isLock) {
                throw new BizIllegalException("请求太频繁");
            }
            return true;
        }
    },
    ;

    public abstract boolean tryLock(RLock lock, MyLock prop) throws InterruptedException;
}
