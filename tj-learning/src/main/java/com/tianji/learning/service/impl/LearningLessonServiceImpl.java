package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.mapper.LearningLessonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author smile67
 * @description 针对表【learning_lesson(学生课程表)】的数据库操作Service实现
 * @createDate 2024-08-07 08:52:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson>
        implements ILearningLessonService {

    final CourseClient courseClient;

    @Override
    public void addUserLesson(Long userId, List<Long> courseIds) {
        // 通过feign远程调用课程服务，查询课程信息
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cinfos)) {
            // 课程不存在，无法添加
            log.error("课程信息不存在，无法添加到课表");
            return;
        }

        // 封装po实体类，填充过期时间
        List<LearningLesson> list = new ArrayList<>();
        for (CourseSimpleInfoDTO cinfo : cinfos) {
            LearningLesson lesson = new LearningLesson();
            Integer validDuration = cinfo.getValidDuration();
            if (validDuration != null && validDuration > 0) {
                LocalDateTime now = LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusMonths(validDuration));
            }
            lesson.setUserId(userId);
            lesson.setCourseId(cinfo.getId());
            list.add(lesson);
        }

        // 批量保存 计算过期时间
        saveBatch(list);
    }
}




