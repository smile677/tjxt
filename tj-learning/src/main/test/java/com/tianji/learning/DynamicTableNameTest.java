package com.tianji.learning;

import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.utils.TableInfoContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest(classes = LearningApplication.class)
class DynamicTableNameTest {
    @Autowired
    private IPointsBoardService pointsBoardService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void test() {
//        TableInfoContext.setInfo("points_board_7");
        PointsBoard pointsBoard = new PointsBoard();
        pointsBoard.setId(1L);
        pointsBoard.setUserId(2L);
        pointsBoard.setPoints(100);
        pointsBoard.setRank(11);
        pointsBoard.setSeason(1);
        pointsBoardService.save(pointsBoard);
    }

    @Test
    void test2() {
        TableInfoContext.setInfo("points_board_7");
        PointsBoard pointsBoard = new PointsBoard();
        pointsBoard.setId(1L);
        pointsBoard.setUserId(2L);
        pointsBoard.setPoints(100);
//        pointsBoard.setRank(11);// 由于动态表名插件的原因，会向points_board_8插入数据，但是points_board_8表中没有该字段
        pointsBoardService.save(pointsBoard);
    }

    @Test
    void test3() {
        // 1.获取上月时间
        LocalDate localDateOfLastMonth = LocalDate.now().minusMonths(1);
        // 2.计算key
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + localDateOfLastMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

        for (int i = 0; i < 20; i++) {
            redisTemplate.opsForZSet().add(key, String.valueOf(i), i);
        }
    }
}
