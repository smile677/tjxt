package com.tianji.learning.service.impl;

import cn.hutool.core.lang.func.Func1;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.mapper.LearningLessonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final LearningRecordMapper learningRecordMapper;
//    private ILearningRecordService learningRecordService; // 会产生循环依赖

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
    public LearningLessonVO queryMyCurrentLesson() {
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

    @Override
    public Long isLessonValid(Long courseId) {
        // 1.获取当前登录用户id
        Long userId = UserContext.getUser();
        // 2.查询课表learning_lesson 条件 user_id course_id
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            return null;
        }
        // 校验课程是否过期
        LocalDateTime expireTime = lesson.getExpireTime();
        LocalDateTime now = LocalDateTime.now();
        if (expireTime != null && now.isAfter(expireTime)) {
            return null;
        }
        return lesson.getId();
    }

    @Override
    public LearningLessonVO queryLessonByCourseId(Long courseId) {
        // 1.获取当前登录用户id
        Long userId = UserContext.getUser();
        // 2.查询课表learning_lesson 条件 user_id course_id
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            return null;
        }
        // 3.封装vo
        return BeanUtils.copyBean(lesson, LearningLessonVO.class);
    }

    @Override
    public void createLearningPlans(LearningPlanDTO learningPlanDTO) {
        // 1. 获取登录用户
        Long userId = UserContext.getUser();
        // 2.查询课表learning_lesson 条件 user_id course_id
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, learningPlanDTO.getCourseId())
                .one();
        if (lesson == null) {
            throw new BadRequestException("该课程没有加入到课表");
        }
        lesson.setWeekFreq(learningPlanDTO.getFreq());
        this.updateById(lesson);
        // 3.修改课表
        boolean update = this.lambdaUpdate()
                // 只会更新一个字段
                .set(LearningLesson::getWeekFreq, learningPlanDTO.getFreq())
                .set(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .eq(LearningLesson::getId, lesson.getId())
                .update();
        if (!update) {
            throw new BadRequestException("创建计划失败");
        }
    }

    @Override
    public LearningPlanPageVO queryMyPlans(PageQuery query) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();

        // TODO 2.查询积分

        // 3.查询本周学习计划总数据 learning_lesson 条件：user_id=2 and plan_status=1 and status in (1,2)
        //select sum(week_freq) from tj_learning.learning_lesson
        //where user_id = 2
        //and plan_status=1
        //and status in (1,2);
        QueryWrapper<LearningLesson> wrapper = new QueryWrapper<>();
        wrapper.select("sum(week_freq) as plansTotal")
                .eq("user_id", userId)
                .in("status", LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .eq("plan_status", PlanStatus.PLAN_RUNNING);
        Map<String, Object> map = this.getMap(wrapper);
        // {plansTotal:7}
        Integer plansTotal = 0;
        if (map != null && map.get("plansTotal") != null) {
            plansTotal = Integer.valueOf(map.get("plansTotal").toString());
        }

        // 4.查询本周已学习计划总数据
        //select count(*) from tj_learning.learning_record
        //where user_id=2
        //and finished=1
        //and finish_time between '2024-08-05 00:00:01' and '2024-08-11 23:59:59';
        LocalDate now = LocalDate.now();
        LocalDateTime weekBeginTime = DateUtils.getWeekBeginTime(now);
        LocalDateTime weekEndTime = DateUtils.getWeekEndTime(now);
        Integer weekFinishedPlanNum = learningRecordMapper.selectCount(Wrappers.<LearningRecord>lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getFinished, true)
                .between(LearningRecord::getFinished, weekBeginTime, weekEndTime));
        // 5.查询课表数据 learning_lessons 条件userId status in(0,1) plan_status=1 分页
        Page<LearningLesson> page = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .in(LearningLesson::getStatus, LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            LearningPlanPageVO vo = new LearningPlanPageVO();
            vo.setTotal(0L);
            vo.setPages(0L);
            vo.setList(CollUtils.emptyList());
        }
        // 6.远程调用课程服务 获得课程信息
        Set<Long> courseIds = records.stream().map(learningLesson -> learningLesson.getCourseId()).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cInfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cInfos)) {
            throw new BizIllegalException("课程不存在");
        }
        // 将cInfos list转换为map <课程id ,CourseSimpleInfoDTO>
        Map<Long, CourseSimpleInfoDTO> cInfoDTOMap = cInfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        // 7.查询学习记录表 本周 当前用户下 每门课下 已学习的小节数量
        //select lesson_id,count(*) from tj_learning.learning_record
        //where user_id=2
        //and finished=1
        //and finish_time between '2024-08-05 00:00:01' and '2024-08-11 23:59:59'
        //group by lesson_id;
        QueryWrapper<LearningRecord> rwrapper = new QueryWrapper<>();
        rwrapper.select("lesson_id,count(*) as userId");
        rwrapper.eq("user_id", userId);
        rwrapper.eq("finished", true);
        rwrapper.between("finish_time", weekBeginTime, weekEndTime);
        rwrapper.groupBy("lesson_id");
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(rwrapper);
        Map<Long, Long> cousreWeekFinishedNumMap = learningRecords.stream().collect(Collectors.toMap(LearningRecord::getLessonId, LearningRecord::getUserId));
        //LearningRecord(id=null, lessonId=1822492577535369217, sectionId=null, userId=1, moment=null, finished=null, createTime=null, finishTime=null, updateTime=null)
        // map中的key是lessonId value是当前用户本周对应的已学习小节数量

        // 8.封装返回vo
        LearningPlanPageVO vo = new LearningPlanPageVO();
        vo.setWeekTotalPlan(plansTotal);
        // todo vo.setWeekPoints();
        vo.setWeekFinished(weekFinishedPlanNum);
        List<LearningPlanVO> voList = new ArrayList<>();
        for (LearningLesson record : records) {
            LearningPlanVO learningPlanVO = BeanUtils.copyBean(record, LearningPlanVO.class);
            CourseSimpleInfoDTO infoDTO = cInfoDTOMap.get(record.getCourseId());
            if (infoDTO != null) {
                // 课程名
                learningPlanVO.setCourseName(infoDTO.getName());
                // 课程下的总小结数
                learningPlanVO.setSections(infoDTO.getSectionNum());
            }
            int cousreWeekFinishedNumOrDefault = cousreWeekFinishedNumMap.getOrDefault(record.getId(), 0L).intValue();
            // 本周已经学章节数
            learningPlanVO.setWeekLearnedSections(cousreWeekFinishedNumOrDefault);
        }
        vo.setList(voList);
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());

        return vo;
    }
}




