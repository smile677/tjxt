package com.tianji;


import com.alibaba.fastjson.JSON;
import com.tianji.api.dto.msg.LikedTimesDTO;
import com.tianji.remark.RemarkApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(classes = RemarkApplication.class)
class RedisTest {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void testPiple1() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            LikedTimesDTO likedTimesDTO = new LikedTimesDTO();
            likedTimesDTO.setBizId(Long.valueOf(i));
            likedTimesDTO.setLikedTimes(i);
            redisTemplate.opsForList().leftPush("1001_1", JSON.toJSONString(likedTimesDTO));
        }
        long end = System.currentTimeMillis();
        System.out.println("使用一般技术执行10000次自增操作共耗时: " + (end - start));
        // 使用一般技术执行10000次自增操作共耗时: 4736
    }

    @Test
    void testPiple2() {
        long start = System.currentTimeMillis();
        // 使用管道技术 适用于短时间内执行大量的 redis 命令
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (int i = 0; i < 10000; i++) {
                    LikedTimesDTO likedTimesDTO = new LikedTimesDTO();
                    likedTimesDTO.setBizId(Long.valueOf(i));
                    likedTimesDTO.setLikedTimes(i);
                    connection.lPush("1001_1".getBytes(), JSON.toJSONString(likedTimesDTO).getBytes());
                }
                return null;
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("使用管道技术执行10000次自增操作共耗时: " + (end - start));
        // 使用管道技术执行10000次自增操作共耗时: 449
    }

}
