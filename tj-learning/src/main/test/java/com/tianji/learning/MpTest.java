package com.tianji.learning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.service.ILearningLessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SpringBootTest(classes = LearningApplication.class)// 测试用例所在的包和引导类所在的包不一样就会报错
public class MpTest {
    @Autowired
    ILearningLessonService learningLessonService;

    @Test
    public void test1() {
        IPage<LearningLesson> page = new Page<>(1, 2);
        LambdaQueryWrapper<LearningLesson> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningLesson::getUserId, 2L).orderByDesc(LearningLesson::getLatestLearnTime);
        learningLessonService.page(page, wrapper);
        System.out.println("page = " + page.getTotal());
        System.out.println("pages = " + page.getPages());
        List<LearningLesson> records = page.getRecords();
        for (LearningLesson record : records) {
            System.out.println("record = " + record);
        }
    }

    @Test
    public void test2() {
        Page<LearningLesson> page = new Page<>(1, 2);
        List<OrderItem> itemList = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setColumn("latest_learn_time");
        item.setAsc(false);
        itemList.add(item);

        page.addOrder(itemList);

        LambdaQueryWrapper<LearningLesson> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningLesson::getUserId, 2L);
//        wrapper.orderByDesc(LearningLesson::getLatestLearnTime);
        learningLessonService.page(page, wrapper);
        System.out.println("page = " + page.getTotal());
        System.out.println("pages = " + page.getPages());
        List<LearningLesson> records = page.getRecords();
        for (LearningLesson record : records) {
            System.out.println("record = " + record);
        }
    }

    @Test
    public void test3() {
        Page<LearningLesson> page = new Page<>(1, 2);
        List<OrderItem> itemList = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setColumn("latest_learn_time");
        item.setAsc(false);
        itemList.add(item);

        page.addOrder(itemList);

//        LambdaQueryWrapper<LearningLesson> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(LearningLesson::getUserId, 2L);
////        wrapper.orderByDesc(LearningLesson::getLatestLearnTime);
//        learningLessonService.page(page, wrapper);

        learningLessonService.lambdaQuery().eq(LearningLesson::getUserId, 2L)
                .page(page);
        System.out.println("page = " + page.getTotal());
        System.out.println("pages = " + page.getPages());
        List<LearningLesson> records = page.getRecords();
        for (LearningLesson record : records) {
            System.out.println("record = " + record);
        }
    }

    @Test
    public void test4() {
        PageQuery query = new PageQuery();
        query.setPageNo(1);
        query.setPageSize(2);
        query.setSortBy("latest_learn_time");
        query.setIsAsc(false);

        Page<LearningLesson> page = learningLessonService.lambdaQuery().eq(LearningLesson::getUserId, 2L)
                .page(query.toMpPage("latest_learn_time", false));
        System.out.println("page = " + page.getTotal());
        System.out.println("pages = " + page.getPages());
        List<LearningLesson> records = page.getRecords();
        for (LearningLesson record : records) {
            System.out.println("record = " + record);
        }
    }

    @Test
    public void test5() {
        List<LearningLesson> list = new ArrayList<>();
        LearningLesson lesson1 = new LearningLesson();
        lesson1.setId(1L);
        lesson1.setUserId(1L);
        lesson1.setCourseId(1L);
        LearningLesson lesson2 = new LearningLesson();
        lesson2.setId(2L);
        lesson2.setUserId(2L);
        lesson2.setCourseId(2L);
        list.add(lesson1);
        list.add(lesson2);
        // 使用steam流获取集合中所有的LearningLesson对象的id值
//        List<Long> ids = new ArrayList<>();
//        for (LearningLesson lesson : list) {
//            ids.add(lesson.getId());
//        }
        // 将对象的属性封装到一个新的集合中
        List<Long> ids = list.stream().map(LearningLesson::getId).collect(Collectors.toList());
        Set<Long> ids2 = list.stream().map(LearningLesson::getId).collect(Collectors.toSet());

        // 使用 stream 流对 list转 map<id, LearningLesson>
        Map<Long, LearningLesson> map = new HashMap<>();
        for (LearningLesson lesson : list) {
            map.put(lesson.getId(), lesson);
        }
        System.out.println("map = " + map);

        Map<Long, LearningLesson> map2 = list.stream().collect(Collectors.toMap(LearningLesson::getId, lesson -> lesson));
        System.out.println("map2 = " + map2);
    }

    @Test
    public void test6() {
        QueryWrapper<LearningLesson> wrapper = new QueryWrapper<>();
        wrapper.select("sum(week_freq) as plansTotal")
                .eq("user_id", 2)
                .in("status", LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .eq("plan_status", PlanStatus.PLAN_RUNNING);
        Map<String, Object> map = learningLessonService.getMap(wrapper);
        System.out.println("map = " + map.get("plansTotal"));
        if (map != null && map.get("plansTotal") != null) {
            Integer a = Integer.valueOf(map.get("plansTotal").toString());
            System.out.println("a = " + a);
            BigDecimal plansTotal = (BigDecimal) map.get("plansTotal");
            int b = plansTotal.intValue();
            System.out.println("b = " + b);
        }
    }

    @Autowired
    private LearningRecordMapper learningRecordMapper;

    @Test
    public void test7() {
        QueryWrapper<LearningRecord> rwrapper = new QueryWrapper<>();
        rwrapper.select("lesson_id,count(*) as userId");
        rwrapper.eq("user_id", 2);
        rwrapper.eq("finished", true);
        rwrapper.between("finish_time", "2024-08-05 00:00:01", "2024-08-11 23:59:59");
        rwrapper.groupBy("lesson_id");
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(rwrapper);
        System.out.println("learningRecords = " + learningRecords);
        Map<Long, Long> cousreWeekFinishedNumMap = learningRecords.stream().collect(Collectors.toMap(LearningRecord::getLessonId, LearningRecord::getUserId));
        System.out.println("cousreWeekFinishedNumMap = " + cousreWeekFinishedNumMap);
    }

    @Autowired
    private IInteractionQuestionService questionService;

    @Test
    public void test8() {
        List<InteractionQuestion> description = questionService.lambdaQuery()
                .select(InteractionQuestion.class, tableFieldInfo -> {
                    // tableFieldInfo.getProperty():获取实体类的属性名称
                    return !Objects.equals(tableFieldInfo.getProperty(), "description");// 制定不查字段
                })
                .eq(InteractionQuestion::getCourseId, "1549025085494521857")
                .eq(InteractionQuestion::getUserId, 2)
                .eq(InteractionQuestion::getHidden, false)
                .orderByDesc(InteractionQuestion::getCreateTime)
                .list();
        for (InteractionQuestion interactionQuestion : description) {
            System.out.println("interactionQuestion = " + interactionQuestion);
        }
    }
}
