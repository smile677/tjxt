package com.tianji.remark.task;

import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务
 * 定时读取点赞总数的变更数据，通过MQ发送给业务方
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikedTimesCheckTask {
    // 业务类型
    private static final List<String> BIZ_TYPES = List.of("QA", "NOTE");
    // 任务每次取的biz数量 防止一次性往mq发送太多消息
    private static final int MAX_BIZ_SIZE = 30;

    private final ILikedRecordService likedRecordService;

    // 每20s执行一次 将redis中 业务类型下面 某业务的点赞总数 发生消息给到mq
    //    @Scheduled(cron = "0/20 * * * * ?")// 每间隔20s执行一次
    @Scheduled(fixedDelay = 20000)// 每间隔20s执行一次
    public void checkLikedTimes() {
        for (String bizType : BIZ_TYPES) {
            likedRecordService.readLikedTimesAndSendMessage(bizType, MAX_BIZ_SIZE);
        }
    }
}
