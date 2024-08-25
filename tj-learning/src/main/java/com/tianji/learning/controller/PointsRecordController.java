package com.tianji.learning.controller;

import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.service.IPointsRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "积分记录相关接口")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsRecordController {
    private final IPointsRecordService pointsRecordService;

    @ApiOperation("查询我的今日积分情况")
    @GetMapping("/today")
    public List<PointsStatisticsVO> queryMyTodayPoints() {
        return pointsRecordService.queryMyTodayPoints();
    }
}
