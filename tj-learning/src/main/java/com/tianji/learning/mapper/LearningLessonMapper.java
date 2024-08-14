package com.tianji.learning.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningLesson;
import org.apache.ibatis.annotations.Mapper;

/**
* @author smile67
* @description 针对表【learning_lesson(学生课程表)】的数据库操作Mapper
* @createDate 2024-08-07 08:52:25
* @Entity generator.domain.LearningLesson
*/
@Mapper
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {

}




