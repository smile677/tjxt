package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.service.IInteractionReplyService;
import com.tianji.learning.mapper.InteractionReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author smile67
 * @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Service实现
 * @createDate 2024-08-14 17:20:57
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply>
        implements IInteractionReplyService {
    private final InteractionQuestionMapper questionMapper;

    @Override
    public void saveReply(ReplyDTO dto) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.保存回答或者评论 interaction_reply
        InteractionReply interactionReply = BeanUtils.copyBean(dto, InteractionReply.class);
        interactionReply.setUserId(userId);
        this.save(interactionReply);
        // 获取问题实体
        InteractionQuestion interactionQuestion = questionMapper.selectById(dto.getQuestionId());
        // 3.判断是否是回答 dto.answerId为空则是回答
        Long answerId = dto.getAnswerId();
        if (answerId != null) {
            // 3.1 如果不是回答是评论 累加回答下面的评论次数
            InteractionReply interactionReply2 = this.getById(answerId);
            interactionReply2.setReplyTimes(interactionReply2.getReplyTimes() + 1);
            this.updateById(interactionReply2);
        } else {
            // 3.2 如果是回答 修改问题表最近一次回答id 同时累加问题表回答次数
            interactionQuestion.setLatestAnswerId(interactionReply.getId());
            interactionQuestion.setAnswerTimes(interactionQuestion.getAnswerTimes() + 1);
        }
        if (dto.getIsStudent()) {
            // 4.判断是否是学生提交 dto.isStudent为true代表是学生 如果是则将问题表中的status字段改为未查看
            interactionQuestion.setStatus(QuestionStatus.UN_CHECK);
        }
        questionMapper.updateById(interactionQuestion);
    }
}




