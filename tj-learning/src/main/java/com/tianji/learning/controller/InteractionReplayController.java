package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation("客户端分页查询回答或者评论列表")
    @GetMapping("/page")
    public PageDTO<ReplyVO> queryReplyVoPage(ReplyPageQuery query){
        return interactionReplyService.queryReplyVoPage(query);
    }
}
