package com.tianji.learning.controller;

import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "赛季相关接口")
@RestController
@RequestMapping("/boards/seasons")
@RequiredArgsConstructor
public class PointsBoardSeasonController {
    private final IPointsBoardSeasonService pointsBoardSeasonService;

    @ApiOperation("查询赛季列表")
    @GetMapping("/list")
    public List<PointsBoardSeason> queryPointsBoardSeasonList() {
        return pointsBoardSeasonService.list();
    }
}
