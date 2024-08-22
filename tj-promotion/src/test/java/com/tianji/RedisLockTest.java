package com.tianji;

import com.tianji.promotion.PromotionApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = PromotionApplication.class)
public class RedisLockTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test() {
        // set nx
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("aBoolean = " + aBoolean);
        Boolean bBoolean = redisTemplate.opsForValue().setIfAbsent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("bBoolean = " + bBoolean);
    }

    @Test
    public void test2() {
        Boolean aBoolean = redisTemplate.opsForValue().setIfPresent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("aBoolean = " + aBoolean);
        Boolean bBoolean = redisTemplate.opsForValue().setIfPresent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("bBoolean = " + bBoolean);
    }

    @Test
    public void test3() {
        Boolean aBoolean = redisTemplate.opsForValue().setIfPresent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("aBoolean = " + aBoolean);
        redisTemplate.opsForValue().set("lock", "wangwu");
        Boolean bBoolean = redisTemplate.opsForValue().setIfPresent("lock", "zhangsan", 20, TimeUnit.SECONDS);
        System.out.println("bBoolean = " + bBoolean);
    }
}
