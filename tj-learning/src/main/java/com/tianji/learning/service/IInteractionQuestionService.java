package com.tianji.learning.service;

import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author smile67
* @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Service
* @createDate 2024-08-14 17:20:57
*/
public interface IInteractionQuestionService extends IService<InteractionQuestion> {

    void saveQuestion(QuestionFormDTO dto);

    void updateQuestion(Long id, QuestionFormDTO dto);
}
