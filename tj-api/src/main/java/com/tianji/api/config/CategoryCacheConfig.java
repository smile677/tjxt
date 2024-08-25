package com.tianji.api.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CategoryClient;
import com.tianji.api.dto.course.CategoryBasicDTO;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Map;

public class CategoryCacheConfig {
    /**
     * 课程分类的caffeine缓存
     */
    @Bean
    public Cache<String, Map<Long, CategoryBasicDTO>> categoryCaches() {
        return Caffeine.newBuilder()
                // 容量限制
                .initialCapacity(1)
                // 最大内存限制
                .maximumSize(10_000)
                // 有效期
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();
    }

    /**
     * 课程分类的缓存工具类
     */
    @Bean // 一般用来声明第三方bean 方法如果有参，会按照类型自动注入
    public CategoryCache categoryCache(
            Cache<String, Map<Long, CategoryBasicDTO>> categoryCaches, CategoryClient categoryClient) {
        return new CategoryCache(categoryCaches, categoryClient);
    }
}
