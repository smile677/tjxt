package com.tianji.learning.controller;

import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author smile67
 */
@Api(tags = "互动提问相关接口")
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class InteractionQuestionController {

    private final IInteractionQuestionService questionService;

    @ApiOperation("新增互动提问")
    @PostMapping
    public void saveQuestion(@Validated @RequestBody QuestionFormDTO dto) {
        questionService.saveQuestion(dto);
    }

    @ApiOperation("修改互动问题")
    @PutMapping("/{id}")
    public void updateQuestion(@PathVariable("id") Long id, @RequestBody QuestionFormDTO dto) {
        questionService.updateQuestion(id, dto);
    }
}
