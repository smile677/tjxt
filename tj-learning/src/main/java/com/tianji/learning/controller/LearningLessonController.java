package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.service.ILearningLessonService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生课表控制器
 */
@RestController
@RequestMapping("/lessons")
@ApiModel(description = "我的课程相关接口")
@RequiredArgsConstructor
public class LearningLessonController {
    final ILearningLessonService learningLessonService;
    @ApiOperation("分页查询我的课程列表")
    @GetMapping("/page")
    public PageDTO<LearningLessonVO> queryMyLesson(PageQuery query){
       return learningLessonService.queryMyLessons(query);
    }
    @ApiOperation("查询正在学习的课程")
    @GetMapping("/now")
    public LearningLessonVO quearyMyCurrentLesson(){
        return learningLessonService.quearyMyCurrentLesson();
    }
}
