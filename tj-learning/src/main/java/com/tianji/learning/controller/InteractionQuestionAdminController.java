package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author smile67
 */
@Api(tags = "互动提问相关接口 - 管理端")
@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class InteractionQuestionAdminController {

    private final IInteractionQuestionService questionService;

    @ApiOperation("分页查询互动问题 - 用户端")
    @GetMapping("/page")
    public PageDTO<QuestionAdminVO> queryQuestionAdminPage(QuestionAdminPageQuery query) {
        return questionService.queryQuestionAdminPage(query);
    }
}
