package com.tianji.learning.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程状态检查定时任务
 * SpringTask定时任务，定期检查learning_lesson表中的课程是否过期，如果过期则将课程状态修改为已过期
 */
@Component
@RequiredArgsConstructor
public class LessonStatusCheckJob {
    private final ILearningLessonService learningLessonService;

    @Scheduled(cron = "0 * * * * * ?")// 每分钟执行一次 域 秒 分 时 日 月 周几 (年)
//    @Scheduled(cron = "0 0 0 * * ?")// 每天凌晨执行一次
    public void lessonStatusCheck() {
        // 1.检查课程状态 未过期的 课程 不区分用户
        List<LearningLesson> list = learningLessonService.list(Wrappers.<LearningLesson>lambdaQuery()
                .ne(LearningLesson::getStatus, LessonStatus.EXPIRED));
        // 2.判断是否过期 过期时间是不是小于当前时间
        for (LearningLesson learningLesson : list) {
            if (learningLesson.getExpireTime().isBefore(LocalDateTime.now())) {
                learningLesson.setStatus(LessonStatus.EXPIRED);
            }
        }
        // 3.判断 批量更新
        learningLessonService.updateBatchById(list);
    }
}
