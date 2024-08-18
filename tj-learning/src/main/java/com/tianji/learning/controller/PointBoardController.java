package com.tianji.learning.controller;

import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.service.IPointsBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "积分排行榜相关接口")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class PointBoardController {
    private final IPointsBoardService pointsBoardService;

    @ApiOperation("查询学霸积分榜 - 当前赛季和历史赛季")
    @GetMapping
    public PointsBoardVO queryPointsBoardList(PointsBoardQuery query) {
        return pointsBoardService.queryPointsBoardList(query);
    }
}
