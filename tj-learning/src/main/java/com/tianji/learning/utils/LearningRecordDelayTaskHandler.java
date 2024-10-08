package com.tianji.learning.utils;

import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author smile67
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningRecordDelayTaskHandler {
    private final DelayQueue<DelayTask<RecordTaskData>> queue = new DelayQueue<>();
    private static final String RECORD_KEY_TEMPLATE = "learning:record:{}";
    private final StringRedisTemplate redisTemplate;
    private final ILearningLessonService lessonService;
    private final LearningRecordMapper learningRecordMapper;
    private static volatile boolean begin = true;

    // 建议1: 如果任务是属于CPU运算型任务， 推荐核心线程数为CPU的核数
    // 建议2: 如果任务是属于IO型的任务，推荐核心线程数为CPU核数的2倍
    static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            24,
            25,
            // 临时空闲线程存活时间
            60,
            TimeUnit.SECONDS,
            // 延迟阻塞队列
            new LinkedBlockingDeque<>(10));

    @PostConstruct // 项目启动后 当前类实例化 属性注入之后 方法会运行 一般用来做初始化工作
    public void init() {
        log.debug("开启学习记录处理的延迟任务");
        // 异步并发类 作用: 开启一个新线程执行任务
        CompletableFuture.runAsync(this::handleDelayTask);
    }

    @PreDestroy // 当前类的实例 销毁之前 该方法会运行 一般用来做销毁工作
    public void destroy() {
        log.debug("关闭学习记录处理的延迟任务");
        begin = false;
    }

    private void handleDelayTask() {
        while (begin) {
            try {
                // 1. 尝试获取任务 // 从队列中拉去任务 take阻塞方法
                DelayTask<RecordTaskData> task = queue.take();
                poolExecutor.submit(() -> {
                    RecordTaskData data = task.getData();
                    // 2.读取Redis缓存
                    LearningRecord learningRecord = readRecordCache(data.getLessonId(), data.getSectionId());
                    log.debug("获取到要处理的播放记录任务 任务数据{} 缓存中的数据{}", data, learningRecord);
                    if (learningRecord == null) {
                        return;
                    }
                    // 3.比较数据
                    if (!Objects.equals(learningRecord.getMoment(), data.getMoment())) {
                        // 4.如果不一致，播放进度在变化，无需持久化
                        return;
                    }
                    // 5.如果一致，证明用户离开了视频，需要持久化
//            learningRecord.setFinished(null);
                    // 5.1.更新学习记录
                    learningRecordMapper.updateById(learningRecord);
                    // 5.2.更新课表
                    LearningLesson lesson = new LearningLesson();
                    lesson.setId(data.getLessonId());
                    lesson.setLatestSectionId(data.getSectionId());
                    lesson.setLatestLearnTime(LocalDateTime.now());
                    lessonService.updateById(lesson);

                    log.debug("准备持久化学习记录信息");
                });
            } catch (InterruptedException e) {
                log.error("处理播放记录任务发生异常", e);
            }
        }
    }

    public void addLearningRecordTask(LearningRecord learningRecord) {
        // 1.添加数据到Redis缓存
        writeRecordCache(learningRecord);
        // 2.提交延迟任务到延迟队列 DelayQueue
        queue.add(new DelayTask<>(new RecordTaskData(learningRecord), Duration.ofSeconds(20)));
    }

    public void writeRecordCache(LearningRecord learningRecord) {
        log.debug("更新学习记录的缓存数据");
        try {
            // 1.数据转换
            String json = JsonUtils.toJsonStr(new RecordCacheData(learningRecord));
            // 2.写入Redis
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, learningRecord.getLessonId());
            redisTemplate.opsForHash().put(key, learningRecord.getSectionId().toString(), json);
            // 3.添加缓存过期时间
            redisTemplate.expire(key, Duration.ofMinutes(1));
        } catch (Exception e) {
            log.error("更新学习记录缓存异常", e);
        }
    }

    public LearningRecord readRecordCache(Long lessonId, Long sectionId) {
        try {
            // 1.读取Redis数据
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
            Object cacheData = redisTemplate.opsForHash().get(key, sectionId.toString());
            if (cacheData == null) {
                return null;
            }
            // 2.数据检查和转换
            return JsonUtils.toBean(cacheData.toString(), LearningRecord.class);
        } catch (Exception e) {
            log.error("缓存读取异常", e);
            return null;
        }
    }

    public void cleanRecordCache(Long lessonId, Long sectionId) {
        // 删除数据
        String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
        redisTemplate.opsForHash().delete(key, sectionId.toString());
    }

    @Data
    @NoArgsConstructor
    private static class RecordCacheData {
        private Long id;
        private Integer moment;
        private Boolean finished;

        public RecordCacheData(LearningRecord learningRecord) {
            this.id = learningRecord.getId();
            this.moment = learningRecord.getMoment();
            this.finished = learningRecord.getFinished();
        }
    }

    @Data
    @NoArgsConstructor
    private static class RecordTaskData {
        private Long lessonId;
        private Long sectionId;
        private Integer moment;

        public RecordTaskData(LearningRecord learningRecord) {
            this.lessonId = learningRecord.getLessonId();
            this.sectionId = learningRecord.getSectionId();
            this.moment = learningRecord.getMoment();
        }
    }
}