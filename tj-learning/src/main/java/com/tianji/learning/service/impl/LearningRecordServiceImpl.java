package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
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
}




