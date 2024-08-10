package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.service.ILearningLessonService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生课表控制器
 *
 * @author smile67
 */
@RestController
@RequestMapping("/lessons")
@ApiModel(description = "我的课程相关接口")
@RequiredArgsConstructor
public class LearningLessonController {
    final ILearningLessonService learningLessonService;

    @ApiOperation("分页查询我的课程列表")
    @GetMapping("/page")
    public PageDTO<LearningLessonVO> queryMyLesson(PageQuery query) {
        return learningLessonService.queryMyLessons(query);
    }

    @ApiOperation("查询正在学习的课程")
    @GetMapping("/now")
    public LearningLessonVO quearyMyCurrentLesson() {
        return learningLessonService.queryMyCurrentLesson();
    }


    /**
     * 校验当前用户是否可以学习当前课程
     * @param courseId 课程id
     * @return lessonId 如果用户报名了则返回lessonId，否则返回null
     */
    @ApiOperation("校验当前用户是否可以学习当前课程")
    @GetMapping("/{courseId}/valid")
    public Long isLessonValid(@PathVariable("courseId") Long courseId) {
        return learningLessonService.isLessonValid(courseId);
    }

    @ApiOperation("查询用户课表中制定课程状态")
    @GetMapping("/{courseId}")
    public LearningLessonVO queryLessonByCourseId(@PathVariable("courseId") Long courseId){
        return learningLessonService.queryLessonByCourseId(courseId);
    }
}
