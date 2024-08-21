package com.tianji.promotion.service;

import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author smile67
* @description 针对表【coupon(优惠券的规则信息)】的数据库操作Service
* @createDate 2024-08-19 18:48:06
*/
public interface ICouponService extends IService<Coupon> {

    void saveCoupon(CouponFormDTO dto);
}
