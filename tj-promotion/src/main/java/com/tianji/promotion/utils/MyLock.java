package com.tianji.promotion.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author smile67
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MyLock {
    String name();

    long waitTime() default 1;

    long leaseTime() default -1;

    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 代表锁类型 默认可重入
     *
     * @return
     */
    MyLockType lockType() default MyLockType.RE_ENTRANT_LOCK;

    /**
     * 代表获取锁的失败策略
     * 默认：
     * 重试超时后失败: 有限重试(RETRY_TIMEOUT(boolean isLock=lock.tryLock(5,10,SECONDS)))+抛出异常FAIL(if(!isLock)throw new Exception)
     */
    MyLockStrategy lockStrategy() default MyLockStrategy.FAIL_AFTER_RETRY_TIMEOUT;
}
