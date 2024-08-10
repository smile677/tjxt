package com.tianji.learning.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;

import java.util.List;

/**
* @author smile67
* @description 针对表【learning_lesson(学生课程表)】的数据库操作Service
* @createDate 2024-08-07 08:52:25
*/
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLesson(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    LearningLessonVO quearyMyCurrentLesson();

    Long isLessonValid(Long courseId);

    LearningLessonVO queryLessonByCourseId(Long courseId);
}
