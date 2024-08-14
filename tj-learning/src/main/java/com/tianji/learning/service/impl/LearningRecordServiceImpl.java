package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
import com.tianji.learning.utils.LearningRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author smile67
 * @description 针对表【learning_record(学习记录表)】的数据库操作Service实现
 * @createDate 2024-08-10 21:33:10
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord>
        implements ILearningRecordService {
    private final ILearningLessonService learningLessonService;
    private final CourseClient courseClient;
    private final LearningRecordDelayTaskHandler learningRecordDelayTaskHandler;

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        // 1.获取当前登录用户id
        Long userId = UserContext.getUser();
        // 2.查询课表信息 条件 user_id 和 courseId
        LearningLesson lesson = learningLessonService.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            throw new BizIllegalException("课程未加入课表");
        }
        // 3.查询学习记录 条件 lesson_id 和 user_id
        List<LearningRecord> records = this.lambdaQuery()
                .eq(LearningRecord::getLessonId, lesson.getId())
                .list();
        // 4.封装数据
        LearningLessonDTO learningLessonDTO = new LearningLessonDTO();
        // 课表id
        learningLessonDTO.setId(lesson.getId());
        // 最近学习的小节id
        learningLessonDTO.setLatestSectionId(lesson.getLatestSectionId());
        learningLessonDTO.setRecords(BeanUtils.copyList(records, LearningRecordDTO.class));
        return learningLessonDTO;
    }

    @Override
    public void addLearningRecord(LearningRecordFormDTO recordDTO) {
        // 1.获取当前登录用户id
        Long userId = UserContext.getUser();
        // 2.处理学习记录
        boolean isFinished = false;
        if (recordDTO.getSectionType() == SectionType.VIDEO) {
            // 2.1 提交视频记录
            isFinished = handleVideoRecord(userId, recordDTO);
        } else {
            // 2.2 提交考试记录
            isFinished = handleExamRecord(userId, recordDTO);
        }
        // 如果本小节 不是第一次学完，不用处理课表数据
        if (!isFinished) {
            return;
        }
        // 3.处理课表数据
        handleLearnLessonsChanges(recordDTO);
    }

    // 处理课表相关记录
    private void handleLearnLessonsChanges(LearningRecordFormDTO recordDTO) {
        // 1.查询课表 learning_lesson 条件  lessonId主键
        LearningLesson learningLesson = learningLessonService.getById(recordDTO.getLessonId());
        if (learningLesson == null) {
            throw new BizIllegalException("课表不存在");
        }
        // 2.判断是否第一次学完 isFinished 为 true
        boolean allFinished = false;
        // 3.远程调用课程服务 得到课程信息 小结总数
        CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(learningLesson.getCourseId(), false, false);
        if (cinfo == null) {
            throw new BizIllegalException("课程不存在");
        }
        Integer sectionNum = cinfo.getSectionNum();
        // 4.如果isFinished为true 本小节为第一次学完 判断该用户对课程下全部下节是否学完
        Integer learnedSections = learningLesson.getLearnedSections();
        allFinished = learnedSections + 1 >= sectionNum;

        // 5.更新课表数据
        learningLessonService.lambdaUpdate()
                .set(learningLesson.getStatus() == LessonStatus.NOT_BEGIN, LearningLesson::getStatus, LessonStatus.LEARNING)
                .set(learningLesson.getLearnedSections() == 0, LearningLesson::getStatus, LessonStatus.LEARNING)
                .set(allFinished, LearningLesson::getStatus, LessonStatus.FINISHED)
                .set(LearningLesson::getLatestSectionId, recordDTO.getSectionId())
                .set(LearningLesson::getLatestLearnTime, recordDTO.getCommitTime())
                .set(LearningLesson::getLearnedSections, learningLesson.getLearnedSections() + 1)
//                .setSql(isFinished, "learned_sections = learned_sections + 1")
                .eq(LearningLesson::getId, learningLesson.getId())
                .update();
    }

    // 处理课表数据
    private boolean handleExamRecord(Long userId, LearningRecordFormDTO recordDTO) {
        // 1.dto -> po
        LearningRecord learningRecord = BeanUtils.copyBean(recordDTO, LearningRecord.class);
        learningRecord.setUserId(userId);
        learningRecord.setFinished(true);
        learningRecord.setFinishTime(recordDTO.getCommitTime());
        // 写入数据库
        boolean success = this.save(learningRecord);
        if (!success) {
            throw new DbException("新增考试记录失败");
        }
        return true;
    }

    // 处理视频播放记录
    private boolean handleVideoRecord(Long userId, LearningRecordFormDTO recordDTO) {
        // 1.查询旧的学习记录 learning_record 条件 lessonId sectionId
        LearningRecord oldLearningRecord = queryOldLearningRecord(recordDTO);
        // 2.判断是否存在
        if (oldLearningRecord == null) {
            // 3.如果不存在则新增学习记录
            LearningRecord learningRecord = BeanUtils.copyBean(recordDTO, LearningRecord.class);
            learningRecord.setUserId(userId);
            boolean result = this.save(learningRecord);
            if (!result) {
                throw new DbException("新增学习记录失败");
            }
            return false;
        }
        // 4.如果存在则更新学习记录 learning_record 更新 moment 字段
        // 是否第一次完成(旧状态为未完成 + 本次学习进度超过 50% 为学完)
        boolean isFinished = !oldLearningRecord.getFinished() && recordDTO.getMoment() * 2 >= recordDTO.getDuration();
        if (!isFinished) {
            LearningRecord learningRecord = new LearningRecord();
            learningRecord.setId(oldLearningRecord.getId());
            learningRecord.setLessonId(recordDTO.getLessonId());
            learningRecord.setSectionId(recordDTO.getSectionId());
            learningRecord.setMoment(recordDTO.getMoment());
            learningRecord.setFinished(isFinished);
            learningRecordDelayTaskHandler.addLearningRecordTask(learningRecord);
            return false;
        }
        boolean result = this.lambdaUpdate()
                .set(LearningRecord::getMoment, recordDTO.getMoment())
                .set(LearningRecord::getFinished, true)
                .set(LearningRecord::getFinishTime, recordDTO.getCommitTime())
                .eq(LearningRecord::getId, oldLearningRecord.getId())
                .update();
        if (!result) {
            throw new DbException("更新视频学习记录失败");
        }
        // 5.清理缓存
        learningRecordDelayTaskHandler.cleanRecordCache(recordDTO.getLessonId(), recordDTO.getSectionId());

        return true;
    }

    private LearningRecord queryOldLearningRecord(LearningRecordFormDTO recordDTO) {
        // 1. 查询缓存
        LearningRecord learningRecordCache = learningRecordDelayTaskHandler.readRecordCache(recordDTO.getLessonId(), recordDTO.getSectionId());
        // 2.如果命中直接返回
        if (learningRecordCache != null) {
            return learningRecordCache;
        }
        // 3.如果没有命中 查询DB
        LearningRecord dbRecord = this.lambdaQuery()
                .eq(LearningRecord::getLessonId, recordDTO.getLessonId())
                .eq(LearningRecord::getSectionId, recordDTO.getSectionId())
//                .eq(LearningRecord::getMoment, recordDTO.getMoment())
                .one();
        if (dbRecord == null) {
            return null;
        }
        // 4.更新缓存
        dbRecord.setMoment(recordDTO.getMoment());
        learningRecordDelayTaskHandler.writeRecordCache(dbRecord);
        return dbRecord;
    }
}




