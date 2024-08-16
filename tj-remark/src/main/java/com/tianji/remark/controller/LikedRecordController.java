package com.tianji.remark.controller;

import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.service.ILikedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "点赞相关接口")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikedRecordController {
    private final ILikedRecordService likedRecordService;

    @ApiOperation("点赞或取消点赞")
    @PostMapping
    public void addLikeRecord(@RequestBody @Validated LikeRecordFormDTO dto) {
        likedRecordService.addLikeRecord(dto);
    }
}
