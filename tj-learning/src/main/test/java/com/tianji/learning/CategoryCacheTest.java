package com.tianji.learning;

import com.tianji.api.cache.CategoryCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = LearningApplication.class)
class CategoryCacheTest {
    @Autowired
    CategoryCache categoryCache;
    @Test
    void test(){
        String categoryNames = categoryCache.getCategoryNames(List.of(1001L, 2001L, 3003L));
        System.out.println("categoryNames = " + categoryNames);
    }
}
