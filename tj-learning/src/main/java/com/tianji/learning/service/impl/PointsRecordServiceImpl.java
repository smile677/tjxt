package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import com.tianji.learning.mapper.PointsRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author smile67
 * @description 针对表【points_record(学习积分记录，每个月底清零)】的数据库操作Service实现
 * @createDate 2024-08-17 23:10:07
 */
@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord>
        implements IPointsRecordService {

    private final StringRedisTemplate redisTemplate;
    @Override
    public void addPointRecord(SignInMessage msg, PointsRecordType pointsRecordType) {
        // 校验
        if (msg == null || pointsRecordType == null) {
            return;
        }
        int realPoints = msg.getPoints();
        // 1.判断该积分类型是否为有上限, type.maxPoints是否大于0
        int maxPoints = pointsRecordType.getMaxPoints();
        if (maxPoints > 0) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
            LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);
            // 2.如果有上限 查询该用户该积分类型 今日已得积分points_record 条件userid type 今天 sum
            // mybatis-plus实现 sql:select sum(points) as totalPoints from points_record where user_id = and type= and create_time between xxx xxx
            QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
            wrapper.select("sum(points) as totalPoints");
            wrapper.eq("user_id", msg.getUserId());
            wrapper.eq("type", pointsRecordType.getValue());
            wrapper.between("create_time", dayStartTime, dayEndTime);
            Map<String, Object> map = this.getMap(wrapper);
            int currentPoints = 0;
            if (map != null) {
                BigDecimal totalPoints = (BigDecimal) map.get("totalPoints");
                currentPoints = totalPoints.intValue();
            }
            // 3.判断已得积分是否超过上线
            if (currentPoints >= maxPoints) {
                // 说明已得积分 已经达到上限
                return;
            }
            // 计算本次实际应该增加多少分
            if (currentPoints + realPoints > maxPoints) {
                realPoints = maxPoints - currentPoints;
            }
        }
        // 4.保存积分
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setPoints(realPoints);
        pointsRecord.setType(pointsRecordType);
        pointsRecord.setUserId(msg.getUserId());
        this.save(pointsRecord);
        // 5.累加并保存积分值到Redis 采用zset 当前赛季的排行榜
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + format;
        redisTemplate.opsForZSet().incrementScore(key, msg.getUserId().toString(), realPoints);
    }

    @Override
    public List<PointsStatisticsVO> queryMyTodayPoints() {
        // 1.获取用户
        Long userId = UserContext.getUser();

        // 2.查询积分points_record
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        // SQL:select type,sum(points) from points_record
        // where user_id=2
        // and points_record.create_time between '2024-08-05 00:00:01' and '2024-08-19 23:59:59'
        // group by type;
        // 利用 userId 字段暂存数据
        wrapper.select("type", "sum(points) as userId");
        wrapper.eq("user_id", userId);
        wrapper.between("create_time", DateUtils.getDayStartTime(LocalDateTime.now()), DateUtils.getDayEndTime(LocalDateTime.now()));
        wrapper.groupBy("type");
        List<PointsRecord> list = this.list(wrapper);
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }

        // 3.封装vo返回
        List<PointsStatisticsVO> voList = new ArrayList<>();
        for (PointsRecord pointsRecord : list) {
            PointsStatisticsVO vo = new PointsStatisticsVO();
            // 积分类型的中文
            vo.setType(pointsRecord.getType().getDesc());
            // 积分值
            vo.setPoints(pointsRecord.getUserId().intValue());
            // 积分类型的上限
            vo.setMaxPoints(pointsRecord.getType().getMaxPoints());
            voList.add(vo);
        }
        return voList;
    }
}




