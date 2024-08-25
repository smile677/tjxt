package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author smile67
 * @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Service实现
 * @createDate 2024-08-14 17:20:57
 */
@Service
@AllArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion>
        implements IInteractionQuestionService {
    private final IInteractionReplyService interactionReplyService;
    private final UserClient userClient;
    private final SearchClient searchClient;
    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;

    @Override
    public void saveQuestion(QuestionFormDTO dto) {
        // 1.获取当前登录用户id
        Long userId = UserContext.getUser();
        // 2.dto转换为po
        InteractionQuestion interactionQuestion = BeanUtils.copyBean(dto, InteractionQuestion.class);
        interactionQuestion.setUserId(userId);
        // 3.保存
        this.save(interactionQuestion);
    }

    @Override
    public void updateQuestion(Long id, QuestionFormDTO dto) {
        // 1.校验
        if (StringUtils.isBlank(dto.getTitle())
                || StringUtils.isBlank(dto.getDescription())
                || dto.getAnonymity() == null) {
            throw new BadRequestException("非法参数");
        }
        // 校验id
        InteractionQuestion interactionQuestion = this.getById(id);
        if (interactionQuestion == null) {
            throw new BadRequestException("非法参数");
        }
        // 只能修改自己的互动问题
        Long userId = UserContext.getUser();
        if (!interactionQuestion.getUserId().equals(userId)) {
            throw new BadRequestException("不能修改别人的互动问题");
        }
        // 2.dto转换为po
        interactionQuestion.setTitle(dto.getTitle());
        interactionQuestion.setDescription(dto.getDescription());
        interactionQuestion.setAnonymity(dto.getAnonymity());
        // 3.更新
        this.updateById(interactionQuestion);
    }

    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        // 1. 校验 参数courseId
        if (query.getCourseId() == null) {
            throw new BadRequestException("非法参数");
        }
        // 2. 获取登录用户id
        Long userId = UserContext.getUser();
        // 3. 分页查询互动问题interaction_question 条件：courserId onlyMine为true才会加userId 小节id不能为空 hidden为false 分页查询 按照提问时间倒叙
        Page<InteractionQuestion> page = this.lambdaQuery()
                // 排除字段：description 减少数据量
                .select(InteractionQuestion.class, tableFieldInfo -> !Objects.equals(tableFieldInfo.getProperty(), "description"))
                .eq(InteractionQuestion::getCourseId, query.getCourseId())
                .eq(query.getSectionId() != null, InteractionQuestion::getSectionId, query.getSectionId())
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, userId)
                .eq(InteractionQuestion::getHidden, false)
                .orderByDesc(InteractionQuestion::getCreateTime)
                .page(query.toMpPage());
        List<InteractionQuestion> interactionQuestionRecords = page.getRecords();
        if (CollUtils.isEmpty(interactionQuestionRecords)) {
            return PageDTO.empty(page.getTotal(), page.getPages());
        }

        // 互动问题的 最新回答id集合
        Set<Long> latestAnswerIds = new HashSet<>();
        // 互动问题的用户id集合
        Set<Long> userIds = new HashSet<>();
        for (InteractionQuestion interactionQuestion : interactionQuestionRecords) {
            // 如果用户是匿名提问，则不显示用户名和头像 也就不再需要去查用户模块了
            if (!interactionQuestion.getAnonymity()) {
                userIds.add(interactionQuestion.getUserId());
            }
            if (interactionQuestion.getLatestAnswerId() != null) {
                latestAnswerIds.add(interactionQuestion.getLatestAnswerId());
            }
        }
//        Set<Long> latestAnswerIds = records
//                .stream()
//                .filter(record -> record.getLatestAnswerId() != null)
//                .map(InteractionQuestion::getLatestAnswerId).collect(Collectors.toSet());

        // 4.根据最新回答id 批量查询回答信息
        Map<Long, InteractionReply> replyMap = new HashMap<>();
        // 虽然前面加入元素的时候已经判空，此处的判空是避免 latestAnswerIds Set整个集合为空
        if (CollUtils.isNotEmpty(latestAnswerIds)) {
//            List<InteractionReply> interactionReplies = interactionReplyService.listByIds(latestAnswerIds);
            List<InteractionReply> interactionReplies = interactionReplyService.list(Wrappers.<InteractionReply>lambdaQuery()
                    .in(InteractionReply::getId, latestAnswerIds)
                    // 管理员才能操作此字段
                    .eq(InteractionReply::getHidden, false));
            for (InteractionReply interactionReply : interactionReplies) {
                if (!interactionReply.getAnonymity()) {
                    // 将最新回答的用户id 存入userIds
                    userIds.add(interactionReply.getUserId());
                }
                replyMap.put(interactionReply.getId(), interactionReply);
            }
//            Map<Long, InteractionReply> interactionReplyMap = interactionReplies.stream().collect(Collectors.toMap(InteractionReply::getId, c -> c));
        }

        // 5.远程调用用户服务 获取用户信息 批量
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));

        // 6.封装成vo
        ArrayList<QuestionVO> questionVOList = new ArrayList<>();
        for (InteractionQuestion interactionQuestion : interactionQuestionRecords) {
            QuestionVO questionVO = BeanUtils.copyBean(interactionQuestion, QuestionVO.class);
            // 不匿名
            if (!questionVO.getAnonymity()) {
                UserDTO userDTO = userDTOMap.get(interactionQuestion.getUserId());
                if (userDTO != null) {
                    // 1.
                    questionVO.setUserName(userDTO.getName());
                    // 2.
                    questionVO.setUserIcon(userDTO.getIcon());
                }
            }
            InteractionReply interactionReply = replyMap.get(interactionQuestion.getLatestAnswerId());
            if (interactionReply != null) {
                // 最新回答如果是非匿名才设置 最新回答者的昵称
                if (!interactionReply.getAnonymity()) {
                    UserDTO userDTO = userDTOMap.get(interactionReply.getUserId());
                    if (userDTO != null) {
                        // 最新回答的内容
                        // 3.
                        questionVO.setLatestReplyUser(userDTO.getName());
                    }
                }
                // 最新回答者的昵称
                // 4.
                questionVO.setLatestReplyContent(interactionReply.getContent());
            }
            questionVOList.add(questionVO);
        }
        return PageDTO.of(page, questionVOList);
    }

    @Override
    public QuestionVO queryQuesionById(Long id) {
        // 1.校验
        if (id == null) {
            throw new BadRequestException("非法参数，问题id不能为空");
        }
        // 2.查询互动问题表 按照主键查询
        InteractionQuestion interactionQuestion = this.getById(id);
        if (interactionQuestion == null) {
            throw new BadRequestException("问题不存在");
        }
        // 3.如果该问题管理员设置了隐藏字段 返回空
        if (interactionQuestion.getHidden()) {
            return null;
        }
        // 4.封装vo返回
        QuestionVO questionVO = BeanUtils.copyBean(interactionQuestion, QuestionVO.class);
        // 5.如果用户是匿名提问，则不显示用户名和头像 也就不再需要去查用户模块了
        if (!interactionQuestion.getAnonymity()) {
            UserDTO userDTO = userClient.queryUserById(interactionQuestion.getUserId());
            if (userDTO != null) {
                questionVO.setUserName(userDTO.getName());
                questionVO.setUserIcon(userDTO.getIcon());
            }
        }
        return questionVO;
    }

    @Override
    public PageDTO<QuestionAdminVO> queryQuestionAdminPage(QuestionAdminPageQuery query) {
        // 0.如果用户传了课程的名称参数，则从es中获取该名称对应的课程id
        String courseName = query.getCourseName();
        List<Long> cids = null;
        if (StringUtils.isNotBlank(courseName)) {
            cids = searchClient.queryCoursesIdByName(courseName);
            if (CollUtils.isEmpty(cids)) {
                return PageDTO.empty(0L, 0L);
            }
        }
        // 1.查询互动问题表 条件前端传了条件就添加 分页 排序 按照提问时间倒序排序
        Page<InteractionQuestion> page = this.lambdaQuery()
                .in(cids != null, InteractionQuestion::getCourseId, cids)
                .eq(query.getStatus() != null, InteractionQuestion::getStatus, query.getStatus())
                .between(query.getBeginTime() != null && query.getEndTime() != null,
                        InteractionQuestion::getCreateTime,
                        query.getBeginTime(), query.getEndTime())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 用户id集合
        Set<Long> uIds = new HashSet<>();
        // 课程id集合
        Set<Long> courseIds = new HashSet<>();
        // 章和节id集合
        Set<Long> chapterAndSectionIds = new HashSet<>();
        for (InteractionQuestion interactionQuestion : records) {
            uIds.add(interactionQuestion.getUserId());
            courseIds.add(interactionQuestion.getCourseId());
            chapterAndSectionIds.add(interactionQuestion.getChapterId());
            chapterAndSectionIds.add(interactionQuestion.getSectionId());
        }

        // 2.远程调用用户服务 获取用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(uIds);
        if (CollUtils.isEmpty(userDTOS)) {
            throw new BizIllegalException("用户不存在");
        }
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));

        // 3.远程调用课程服务 获取课程信息
        List<CourseSimpleInfoDTO> courseSimpleInfoDTOList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(courseSimpleInfoDTOList)) {
            throw new BizIllegalException("课程不存在");
        }
        Map<Long, CourseSimpleInfoDTO> courseSimpleInfoDTOMap = courseSimpleInfoDTOList.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        // 4.远程调用课程服务 获取章节信息
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(chapterAndSectionIds);
        if (CollUtils.isEmpty(cataSimpleInfoDTOS)) {
            throw new BizIllegalException("章节或者目录不存在");
        }
        Map<Long, String> cataInfoDTOMap = cataSimpleInfoDTOS.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId, c -> c.getName()));


        // 6.封装vo放回
        List<QuestionAdminVO> questionAdminVOList = new ArrayList<>();
        for (InteractionQuestion interactionQuestion : records) {
            QuestionAdminVO questionAdminVO = BeanUtils.copyBean(interactionQuestion, QuestionAdminVO.class);
            UserDTO userDTO = userDTOMap.get(interactionQuestion.getUserId());
            if (userDTO != null) {
                questionAdminVO.setUserName(userDTO.getName());
            }
            CourseSimpleInfoDTO infoDTO = courseSimpleInfoDTOMap.get(interactionQuestion.getCourseId());
            if (infoDTO != null) {
                questionAdminVO.setCourseName(infoDTO.getName());
                List<Long> categoryIds = infoDTO.getCategoryIds();
                // 获取三级分类名称(数据量小+变动小：本地Caffeine缓存)
                String categoryNames = categoryCache.getCategoryNames(categoryIds);
                questionAdminVO.setCategoryName(categoryNames);
            }
            questionAdminVO.setChapterName(cataInfoDTOMap.get(interactionQuestion.getChapterId()));
            questionAdminVO.setSectionName(cataInfoDTOMap.get(interactionQuestion.getSectionId()));
            questionAdminVOList.add(questionAdminVO);
        }
        return PageDTO.of(page, questionAdminVOList);
    }
}
