package com.tianji.learning.controller;

import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.service.ISignRecordService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 签到控制器
 *
 * @author smile67
 */
@Slf4j
@RestController
@RequestMapping("/sign-records")
@RequiredArgsConstructor
public class SignRecordsController {
    private final ISignRecordService signRecordService;

    @ApiOperation("签到")
    @PostMapping
    public SignResultVO addSignRecords() {
        return signRecordService.addSignRecords();
    }

    @ApiOperation("查询签到记录")
    @GetMapping
    public List<Long> getAllSignRecords() {
        return signRecordService.getAllSignRecords();
    }
}
