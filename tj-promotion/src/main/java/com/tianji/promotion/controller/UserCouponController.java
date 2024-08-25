package com.tianji.promotion.controller;

import com.tianji.promotion.domain.dto.CouponDiscountDTO;
import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.service.IUserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户卷相关接口")
@RestController
@RequestMapping("/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {
    private final IUserCouponService userCouponService;

    @ApiOperation("领取优惠券")
    @PostMapping("{id}/receive")
    public void receiveCoupon(@PathVariable Long id) {
        userCouponService.receiveCoupon(id);
    }

    @ApiOperation("兑换码兑换优惠券")
    @PostMapping("/{code}/exchange")
    public void exchangeCoupon(@PathVariable String code) {
        userCouponService.exchangeCoupon(code);
    }
    // 该方法是给tj-trade服务调用的 远程调用使用

    /**
     * 查询可用的优惠券方案
     *
     * @param courseDTOS 订单中的课程信息
     * @return 方案集合
     */
    @ApiOperation("查询可以优惠券方案")
    @PostMapping("/available")
    public List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> courseDTOS) {
        return userCouponService.findDiscountSolution(courseDTOS);
    }
}
