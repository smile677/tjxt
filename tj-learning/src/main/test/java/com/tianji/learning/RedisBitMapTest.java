package com.tianji.learning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@SpringBootTest(classes = LearningApplication.class)
class RedisBitMapTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test() {
        // 对test116 第5天 做签到
        // 返回结果代表offset为false 代表原来的值
        Boolean setBit = stringRedisTemplate.opsForValue().setBit("test116", 4, true);
        System.out.println("setBit = " + setBit);
        if (setBit) {
            // 说明已经签过到了
            // 抛出异常
        }
    }

    @Test
    void test2() {
        // 取第一天到第三天 签到记录 redis的bitmap存的是二进制 取出来的是十进制
        // bitfield test116 get u3 0 取test116 从第一天开始取 取3位转换为无符号十进制
        List<Long> list = stringRedisTemplate.opsForValue().bitField("test116",
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(3)).valueAt(0));
        assert list != null;
        Long value = list.get(0);
        System.out.println("value = " + value);
    }
}
