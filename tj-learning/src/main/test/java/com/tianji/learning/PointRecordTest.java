package com.tianji.learning;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianji.common.utils.DateUtils;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.service.IPointsRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@SpringBootTest(classes = LearningApplication.class)
class PointRecordTest {
    @Autowired
    IPointsRecordService pointsRecordService;

    @Test
    void test() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
        System.out.println("dayStartTime = " + dayStartTime);
        LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);
        System.out.println("dayEndTime = " + dayEndTime);
        // 2.如果有上限 查询该用户该积分类型 今日已得积分points_record 条件userid type 今天 sum
        // mybatis-plus实现 sql:select sum(points) as totalPoints from points_record where user_id = and type= and create_time between xxx xxx
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        wrapper.select("sum(points) as totalPoints");
        wrapper.eq("user_id", 2);
        wrapper.eq("type", 2);
        wrapper.between("create_time", dayStartTime, dayEndTime);
        Map<String, Object> map = pointsRecordService.getMap(wrapper);
        if (map != null) {
            BigDecimal totalPoints = (BigDecimal) map.get("totalPoints");
            System.out.println("totalPoints = " + totalPoints);
        }
    }
}
