package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.mapper.LearningLessonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final CatalogueClient catalogueClient;

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

    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        // 获取当前登录人
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("必须登录");
        }
        // 分页查询我的课表
        Page<LearningLesson> page = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 远程调用课程服务，给vo中的课程名、封面、章节数赋值
        Set<Long> courseIds = records.stream().map(LearningLesson::getUserId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cinfos)) {
            throw new BizIllegalException("课程不存在");
        }
        // 将cinfos课程集合转换为map结构<课程id,课程对象>
        Map<Long, CourseSimpleInfoDTO> infoDTOMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, cinfo -> cinfo));
        // 将po中的数据封装到vo中
        List<LearningLessonVO> voList = new ArrayList<>();
        for (LearningLesson learningLessonRecord : records) {
            LearningLessonVO learningLessonVO = BeanUtils.copyBean(learningLessonRecord, LearningLessonVO.class);
            CourseSimpleInfoDTO infoDTO = infoDTOMap.get(learningLessonRecord.getCourseId());
            if (infoDTO != null) {
                learningLessonVO.setCourseCoverUrl(infoDTO.getCoverUrl());
                learningLessonVO.setCourseName(infoDTO.getName());
                learningLessonVO.setSections(infoDTO.getSectionNum());
            }
            voList.add(learningLessonVO);
        }
        // 返回
        return PageDTO.of(page, voList);
    }

    @Override
    public LearningLessonVO quearyMyCurrentLesson() {
        // 获取当前登录用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("必须登录~");
        }
        // 查询当前用户最近学习课程 按照latest_learn_time 降序排列 取第一条 正在学习的课程 status=1
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING)
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();
        if (lesson == null) {
            return null;
        }

        // 远程调用课程服务，给vo中的课程名、封面、章节数赋值
        CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cinfo == null) {
            throw new BizIllegalException("课程不存在");
        }

        // 查询当前用户课表中 总的课程数
        // select count(*) from learning_lesson where user_id=?
        Integer count = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .count();

        // 通用feign 远程调用课程服务 获取小结名称 和小结节编号
        Long latestSectionId = lesson.getLatestSectionId();
        List<Long> sIds = new ArrayList<>();
        sIds.add(latestSectionId);
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(sIds);
        if (CollUtils.isEmpty(cataSimpleInfoDTOS)) {
            throw new BizIllegalException("小节不存在");
        }

        // 封装返回vo
        LearningLessonVO learningLessonVO = BeanUtils.copyBean(lesson, LearningLessonVO.class);
        learningLessonVO.setCourseName(cinfo.getName());
        learningLessonVO.setCourseCoverUrl(cinfo.getCoverUrl());
        learningLessonVO.setSections(cinfo.getSectionNum());
        // 当前用户能学习的课程总数
        learningLessonVO.setCourseAmount(count);
        CataSimpleInfoDTO cataSimpleInfoDTO = cataSimpleInfoDTOS.get(0);
        learningLessonVO.setLatestSectionName(cataSimpleInfoDTO.getName());
        learningLessonVO.setLatestSectionIndex(cataSimpleInfoDTO.getCIndex());

        return learningLessonVO;
    }
}




