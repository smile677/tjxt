package com.tianji.learning.task;

import com.tianji.common.utils.CollUtils;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.utils.TableInfoContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class PointsBoardPersistentHandler {
    private final IPointsBoardSeasonService pointsBoardSeasonService;
    private final IPointsBoardService pointsBoardService;

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

    // 持久化上赛季（上个月）排行榜数据 到db中
    @XxlJob("savePointsBoard2DB")// 任务名字要和xxl-job控制台 任务中的jobhandler保存一致
    public void savePointsBoard2DB() {
        // 1.获取上月 当前时间
        LocalDate localDateOfLastMonth = LocalDate.now().minusMonths(1);
        // 2.查询赛季表points_board_season 获取上赛季信息
        PointsBoardSeason one = pointsBoardSeasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, localDateOfLastMonth)
                .ge(PointsBoardSeason::getEndTime, localDateOfLastMonth)
                .one();
        log.debug("上赛季信息 {}", one);
        if (one == null) {
            log.warn("上赛季积分榜赛季信息为空，不持久化积分榜数据");
            return;
        }
        // 3.计算动态表名 并存入threadlocal中
        String tableName = RedisConstants.POINTS_BOARD_TABLE_PREFIX + one.getId();
        log.debug("持久化上赛季积分榜数据到db中，表名：{}", tableName);
        TableInfoContext.setInfo(tableName);
        // 4.分页获取redis上赛季积分排行榜数据
        String format = localDateOfLastMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // boards:202408
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + format;
        // 当前实例的分片索引，是从0开始的
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总实例
        int shardTotal = XxlJobHelper.getShardTotal();
        int pageNo = 1 + shardIndex;
        int pageSize = 5;
        while (true) {
            log.debug("处理第{}页数据", pageNo);
            List<PointsBoard> pointsBoardList = pointsBoardService.queryCurrentBoard(key, pageNo, pageSize);
            if (CollUtils.isEmpty(pointsBoardList)) {
                break;// 跳出循环 进行下一步
            }
            pageNo += shardTotal;
            // 5.持久化到db相应的赛季表中
            for (PointsBoard pointsBoard : pointsBoardList) {
                pointsBoard.setId(Long.valueOf(pointsBoard.getRank()));
                pointsBoard.setRank(null);
            }
            pointsBoardService.saveBatch(pointsBoardList);
        }
        // 6.清空threadlocal中的数据
        TableInfoContext.remove();
    }
}
