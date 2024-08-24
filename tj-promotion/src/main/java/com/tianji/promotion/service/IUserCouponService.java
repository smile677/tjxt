package com.tianji.promotion.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.promotion.domain.dto.CouponDiscountDTO;
import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.domain.dto.UserCouponDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.UserCoupon;

import java.util.List;

/**
* @author smile67
* @description 针对表【user_coupon(用户领取优惠券的记录，是真正使用的优惠券信息)】的数据库操作Service
* @createDate 2024-08-21 19:02:37
*/
public interface IUserCouponService extends IService<UserCoupon> {

    void receiveCoupon(Long id);

    void exchangeCoupon(String code);

    void checkAndCreateUserCoupon(Long userId, Coupon coupon, Long serialNum);

    void checkAndCreateUserCouponNew(UserCouponDTO msg);

    List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS);
}
