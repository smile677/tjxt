package com.tianji.learning.task;

import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Slf4j
@Component
@RequiredArgsConstructor
public class PointsBoardPersistentHandler {
    private final IPointsBoardSeasonService pointsBoardSeasonService;

    /**
     * 创建上赛季（上个月）榜单表
     */
    // 每月1号凌晨三点运行
//    @Scheduled(cron = "0 0 3 1 * ?")
    // 测试用 下一分钟
//    @Scheduled(cron = "0 1 2 19 8 ?")
    @XxlJob("createTableJob")
    public void createPointsBoardTableOfLastSeason() {
        log.info("创建上赛季积分榜");
        // 1.获取上个月当前时间点
        LocalDate localDateOfLastMonth = LocalDate.now().minusMonths(1);
        // 2.查询赛季表获取赛季id
        PointsBoardSeason one = pointsBoardSeasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, localDateOfLastMonth)
                .ge(PointsBoardSeason::getEndTime, localDateOfLastMonth)
                .one();
        log.debug("上赛季积分榜赛季信息：{}", one);
        if (one == null) {
            log.warn("上赛季积分榜赛季信息为空，不创建积分榜表");
            return;
        }
        // 3.创建积分榜表
        pointsBoardSeasonService.createPointsBoardLastestTable(one.getId());
    }
}
