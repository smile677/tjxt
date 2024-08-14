package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import org.springframework.stereotype.Service;

/**
 * @author smile67
 * @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Service实现
 * @createDate 2024-08-14 17:20:57
 */
@Service
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion>
        implements IInteractionQuestionService {

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
        interactionQuestion.setDescription( dto.getDescription());
        interactionQuestion.setAnonymity(dto.getAnonymity());
        // 3.更新
        this.updateById(interactionQuestion);
    }
}
