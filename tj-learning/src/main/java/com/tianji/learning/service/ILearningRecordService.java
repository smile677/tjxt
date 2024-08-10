package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.po.LearningRecord;

/**
* @author smile67
* @description 针对表【learning_record(学习记录表)】的数据库操作Service
* @createDate 2024-08-10 21:33:10
*/
public interface ILearningRecordService extends IService<LearningRecord> {

    LearningLessonDTO queryLearningRecordByCourse(Long courseId);
}
