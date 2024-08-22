package com.tianji.promotion.utils;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

/**
 * @author smile67
 */
@Aspect
@Component
@RequiredArgsConstructor
public class MyLockAspect implements Order {
    private final RedissonClient redissonClient;
    private final MyLockFactory myLockFactory;

    // 环绕通知@Around 前置 @Before 后置 @After 异常 @AfterThrowing 最终 @AfterReturning
    @Around("@annotation(mylock)")
    public Object around(ProceedingJoinPoint joinPoint, MyLock mylock) throws Throwable {
        // 1.获取锁对象，指定锁名称 可重入
        // 根据工厂模式，获取制定类型的 锁对象 参数：锁类型、锁名称
        RLock lock = myLockFactory.getLock(mylock.lockType(), mylock.name());

        // 2.尝试获取锁，参数：waitTime、leaseTime、时间单位
        boolean isLock = mylock.lockStrategy().tryLock(lock, mylock);

        if (!isLock) {
            return null;
        }
        // 3.判断是否成功
        try {
            return joinPoint.proceed();
        } finally {
            // 4.释放锁
            lock.unlock(); // 底层有校验想要删的锁是否是自己的 即校验和删除两个操作是原子性的
        }
    }

    @Override
    public int value() {
        // return值代表了 执行顺序 值越小优先级越高
        return 0;
    }

    /**
     * Returns the annotation type of this annotation.
     *
     * @return the annotation type of this annotation
     */
    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
