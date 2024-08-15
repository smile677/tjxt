package com.tianji.learning.controller;

import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags ="回答或者评论的相关接口")
@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
public class InteractionReplayController {
    private final IInteractionReplyService interactionReplyService;
    @ApiOperation("新增回答或者评论")
    @PostMapping
    public void saveReply(@Validated @RequestBody ReplyDTO replyDTO){
        interactionReplyService.saveReply(replyDTO);
    }
}
