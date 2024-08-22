package com.tianji;

import com.tianji.promotion.PromotionApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = PromotionApplication.class)
class RedissonTest {

    @Autowired
    RedissonClient redissonClient;

    @Test
    void test() throws InterruptedException {
        // 1.获取锁对象，指定锁名称 可重入
        RLock lock = redissonClient.getLock("anyLock");
        try {
            // 2.尝试获取锁，参数：waitTime、leaseTime、时间单位
//            boolean isLock = lock.tryLock(1, 30, TimeUnit.SECONDS);
            // 看门狗机制不能设置失效时间 采用默认的失效时间30s
            boolean isLock = lock.tryLock();
            if (!isLock) {
                // 获取锁失败处理 ..
            } else {
                // 获取锁成功处理
            }
            // 通过睡眠查看看门狗机制是否生效，不能打断点
            TimeUnit.SECONDS.sleep(60);
        } finally {
            // 4.释放锁
            lock.unlock();
        }
    }
}
