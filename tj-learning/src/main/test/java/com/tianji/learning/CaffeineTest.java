package com.tianji.learning;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
class CaffeineTest {
    @Test
    void testBasicOps() {
        // 构建cache对象
        Cache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(1)
                .initialCapacity(1)
                .build();

        // 存数据
        cache.put("gf", "迪丽热巴");
        cache.put("bf", "张三");

        // 取数据
        String gf = cache.getIfPresent("gf");
        System.out.println("gf = " + gf);
        String bf = cache.getIfPresent("bf");
        System.out.println("bf = " + bf);

        String gf1 = cache.getIfPresent("gf");
        System.out.println("gf1 = " + gf1);
        String bf1 = cache.getIfPresent("bf");
        System.out.println("bf1 = " + bf1);

        // 取数据，包含两个参数：
        // 参数一：缓存的key
        // 参数二：Lambda表达式，表达式参数就是缓存的key，方法体是查询数据库的逻辑
        // 优先根据key查询JVM缓存，如果未命中，则执行参数二的Lambda表达式
        String defaultGF = cache.get("defaultGF", key -> {
            // key不存在的时候才会执行lambda，查询数据库
            System.out.println("caffeine方法的 get 的lambda执行了");
            // 根据key去数据库查询数据
            return "柳岩";
        });
        System.out.println("defaultGF = " + defaultGF);

        String defaultGF1 = cache.get("defaultGF", key -> {
            // 第二次不会再执行次方法，因为缓存中已经有了
            System.out.println("caffeine方法的 get 的lambda执行了");
            // 根据key去数据库查询数据
            return "柳岩";
        });
        System.out.println("defaultGF = " + defaultGF1);
    }
}
